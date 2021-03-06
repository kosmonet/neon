/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
 * 
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neon.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.stage.Stage;
import neon.client.handlers.CollisionHandler;
import neon.client.handlers.EntityHandler;
import neon.client.handlers.FileHandler;
import neon.client.handlers.MessageHandler;
import neon.client.states.ContainerState;
import neon.client.states.ConversationState;
import neon.client.states.CutSceneState;
import neon.client.states.GameState;
import neon.client.states.InventoryState;
import neon.client.states.JournalState;
import neon.client.states.LoadState;
import neon.client.states.MagicState;
import neon.client.states.MainMenuState;
import neon.client.states.MapState;
import neon.client.states.NewGameState;
import neon.client.states.OptionState;
import neon.client.states.TradeState;
import neon.client.states.Transition;
import neon.client.states.TransitionEvent;
import neon.client.ui.UserInterface;
import neon.common.event.InputEvent;
import neon.common.event.NeonEvent;
import neon.common.event.ServerEvent;
import neon.common.files.NeonFileSystem;
import neon.common.files.Permissions;
import neon.common.net.ClientSocket;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.CreatureLoader;
import neon.common.resources.loaders.MapLoader;
import neon.common.resources.loaders.TerrainLoader;
import neon.common.resources.loaders.TextLoader;
import neon.systems.magic.SpellLoader;

/**
 * The neon client.
 * 
 * @author mdriesen
 */
public final class Client implements Runnable {
	private static final Logger LOGGER = Logger.getGlobal();

	private final EventBus bus = new EventBus("Client Bus");
	private final ClientSocket socket;
	private final UserInterface ui;
	private final NeonFileSystem files = new NeonFileSystem(Permissions.READONLY);
	private final ResourceManager resources = new ResourceManager();
	private final ComponentManager components = new ComponentManager();
	private final Configuration config = new Configuration();

	/**
	 * Initializes the client. The socket must not be null.
	 * 
	 * @param version	the current version of the client
	 * @param socket	the socket used for communication with the server
	 * @param stage		the JavaFX stage used for drawing the user interface
	 */
	public Client(String version, ClientSocket socket, Stage stage) {
		// add all required listeners to the event bus
		this.socket = Objects.requireNonNull(socket, "client socket");
		bus.register(socket);
		bus.register(this);
		ui = new UserInterface(stage);
		bus.register(ui);
		
		// server should cleanly shut down if client is closed
		stage.setOnCloseRequest(event -> bus.post(new InputEvent.Quit()));
		
		// initialize file system
		try {
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			LOGGER.severe("could not initialize temporary folder in file system");			
		}
		
		// add all loaders to the resource manager
		resources.addLoader(new TerrainLoader(files));
		resources.addLoader(new CreatureLoader(files));
		resources.addLoader(new MapLoader(files));
		resources.addLoader(new ConfigurationLoader(files));
		resources.addLoader(new SpellLoader(files));
		resources.addLoader(new TextLoader(files));
		
		// set up some event handlers
		bus.register(new CollisionHandler(ui, bus, components, config));
		bus.register(new EntityHandler(components, config));
		bus.register(new MessageHandler(ui, components));
		bus.register(new FileHandler(files, components, resources, config));
		
		// initialize all states and enter the first one
		initStates(version);
	}
	
	/**
	 * Continuously tries to receive events from the event queue and post them
	 * on the client event bus.
	 */
	public void run() {
		while (true) {
			NeonEvent event = socket.getEvent();
			Platform.runLater(() -> bus.post(event));
		}
	}
	
	/**
	 * Initializes all the states and transitions in the state machine.
	 * 
	 * @param version
	 */
	private void initStates(String version) {
		// create all states and transitions
		MainMenuState mainMenu = new MainMenuState(ui, version, bus);
		
		OptionState options = new OptionState(ui, bus);
		bus.register(new Transition(mainMenu, options, "options"));
		bus.register(new Transition(options, mainMenu, "cancel"));

		NewGameState newGame = new NewGameState(ui, bus, resources);
		bus.register(new Transition(mainMenu, newGame, "new game"));
		bus.register(new Transition(newGame, mainMenu, "cancel"));
		
		LoadState loadGame = new LoadState(ui, bus);
		bus.register(new Transition(mainMenu, loadGame, "load game"));
		bus.register(new Transition(loadGame, mainMenu, "cancel"));
		
		CutSceneState cut = new CutSceneState(ui, bus, resources);
		bus.register(new Transition(newGame, cut, "start game"));

		GameState game = new GameState(ui, bus, components, resources, config);
		bus.register(new Transition(cut, game, "cancel"));		
		bus.register(new Transition(loadGame, game, "start game"));
		bus.register(game);

		InventoryState inventory = new InventoryState(ui, bus, components, config);
		bus.register(new Transition(game, inventory, "inventory"));
		bus.register(new Transition(inventory, game, "cancel"));

		MapState map = new MapState(ui, bus, resources, config);
		bus.register(new Transition(game, map, "map"));
		bus.register(new Transition(map, game, "cancel"));
		
		ContainerState container = new ContainerState(ui, bus, components, config);
		bus.register(new Transition(game, container, "pick"));
		bus.register(new Transition(container, game, "cancel"));
		
		JournalState journal = new JournalState(ui, bus, components);
		bus.register(new Transition(game, journal, "journal"));
		bus.register(new Transition(journal, game, "cancel"));
		
		ConversationState conversation = new ConversationState(ui, bus, components);
		bus.register(new Transition(game, conversation, "talk"));
		bus.register(new Transition(conversation, game, "cancel"));

		MagicState magic = new MagicState(ui, bus, components, resources);
		bus.register(new Transition(game, magic, "magic"));
		bus.register(new Transition(magic, game, "cancel"));		
		
		TradeState trade = new TradeState(ui, bus);
		bus.register(new Transition(conversation, trade, "trade"));
		bus.register(new Transition(trade, game, "cancel"));		

		// enter the first state
		mainMenu.setActive(true);
		mainMenu.enter(new TransitionEvent("start"));
	}

	/**
	 * Gives a warning when an event is detected that no other object is currently 
	 * listening to.
	 * 
	 * @param event	the {@code DeadEvent} describing the unhandled event
	 */
	@Subscribe
	private void monitor(DeadEvent event) {
		LOGGER.warning("client received a dead event: " + event.getEvent());
	}

	/**
	 * Throws an error when a server event has somehow made its way into the
	 * client.
	 * 
	 * @param event	the {@code ServerEvent}
	 */
	@Subscribe
	private void monitor(ServerEvent event) {
		throw new AssertionError("Client received a server event!");
	}
}
