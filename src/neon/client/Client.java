/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.stage.Stage;

import neon.client.resource.MapLoader;
import neon.client.states.ContainerState;
import neon.client.states.ConversationState;
import neon.client.states.CutSceneState;
import neon.client.states.GameState;
import neon.client.states.InventoryState;
import neon.client.states.JournalState;
import neon.client.states.LoadState;
import neon.client.states.MainMenuState;
import neon.client.states.MapState;
import neon.client.states.NewGameState;
import neon.client.states.OptionState;
import neon.client.states.Transition;
import neon.client.states.TransitionEvent;
import neon.common.event.ClientConfigurationEvent;
import neon.common.event.NeonEvent;
import neon.common.event.QuitEvent;
import neon.common.event.UpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.net.ClientSocket;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ConfigurationLoader;
import neon.common.resources.loaders.CreatureLoader;
import neon.common.resources.loaders.DialogLoader;
import neon.common.resources.loaders.ItemLoader;
import neon.common.resources.loaders.TerrainLoader;
import neon.entity.components.Behavior;
import neon.entity.components.Graphics;
import neon.entity.components.Shape;
import neon.entity.entities.Creature;
import neon.entity.entities.Item;

/**
 * 
 * @author mdriesen
 *
 */
public class Client implements Runnable {
	private static final Logger logger = Logger.getGlobal();

	private final EventBus bus = new EventBus("Client Bus");
	private final ClientSocket socket;
	private final UserInterface ui;
	private final NeonFileSystem files = new NeonFileSystem(NeonFileSystem.READONLY);
	private final ResourceManager resources = new ResourceManager(files);
	private final ComponentManager components = new ComponentManager();

	/**
	 * Initializes the client.
	 * 
	 * @param version	the current version of the client
	 * @param socket	the socket used for communication with the server
	 * @param stage		the JavaFX stage used for drawing the user interface
	 */
	public Client(String version, ClientSocket socket, Stage stage) {
		// add all required listeners to the event bus
		this.socket = socket;
		bus.register(socket);
		bus.register(this);
		ui = new UserInterface(stage);
		bus.register(ui);
		
		// server should cleanly shut down if client is closed
		stage.setOnCloseRequest(event -> bus.post(new QuitEvent()));
		
		// initialize file system
		try {
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			logger.severe("could not initialize temporary folder in file system");			
		}
		
		// add all loaders to the resource manager
		resources.addLoader("terrain", new TerrainLoader());
		resources.addLoader("creatures", new CreatureLoader());
		resources.addLoader("items", new ItemLoader());
		resources.addLoader("dialog", new DialogLoader());
		resources.addLoader("maps", new MapLoader());
		resources.addLoader("config", new ConfigurationLoader());
		
		// initialize all modules and enter the first one
		initModules(version);
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
	
	private void initModules(String version) {
		MainMenuState mainMenu = new MainMenuState(ui, version, bus);
		bus.register(mainMenu);

		NewGameState newGame = new NewGameState(ui, bus, resources);
		bus.register(newGame);
		
		LoadState loadGame = new LoadState(ui, bus);
		bus.register(loadGame);
		
		CutSceneState cut = new CutSceneState(ui, bus, files, resources);
		bus.register(cut);

		GameState game = new GameState(ui, bus, components, resources);
		bus.register(game);
		
		InventoryState inventory = new InventoryState(ui, bus, components);
		bus.register(inventory);
		
		MapState map = new MapState(ui, bus, resources);
		bus.register(map);
		
		ConversationState conversation = new ConversationState(ui, bus);
		bus.register(conversation);
		
		ContainerState container = new ContainerState(ui, bus, components);
		bus.register(container);
		
		JournalState journal = new JournalState(ui, bus, components);
		bus.register(journal);
		
		OptionState options = new OptionState(ui, bus);
		bus.register(options);
		
		// register all state transitions on the bus to listen for transition events
		bus.register(new Transition(mainMenu, newGame, "new game"));
		bus.register(new Transition(newGame, mainMenu, "cancel"));
		bus.register(new Transition(newGame, cut, "start game"));
		
		bus.register(new Transition(cut, game, "cancel"));
		
		bus.register(new Transition(mainMenu, loadGame, "load game"));
		bus.register(new Transition(loadGame, mainMenu, "cancel"));
		bus.register(new Transition(loadGame, game, "start game"));
		
		bus.register(new Transition(mainMenu, options, "options"));
		bus.register(new Transition(options, mainMenu, "cancel"));
		
		bus.register(new Transition(game, inventory, "inventory"));
		bus.register(new Transition(inventory, game, "cancel"));

		bus.register(new Transition(game, map, "map"));
		bus.register(new Transition(map, game, "cancel"));
		
		bus.register(new Transition(game, conversation, "talk"));
		bus.register(new Transition(conversation, game, "cancel"));

		bus.register(new Transition(game, container, "pick"));
		bus.register(new Transition(container, game, "cancel"));
		
		bus.register(new Transition(game, journal, "journal"));
		bus.register(new Transition(journal, game, "cancel"));
		
		// enter the first state
		mainMenu.setActive(true);
		mainMenu.enter(new TransitionEvent("start"));
	}

	@Subscribe
	private void onItemChange(UpdateEvent.Item event) throws ResourceException {
		long uid = event.uid;
		RItem resource = resources.getResource("items", event.id);

		components.putComponent(uid, new Item.Resource(uid, resource));
		components.putComponent(uid, new Graphics(uid, resource.glyph, resource.color));
		components.putComponent(uid, new Shape(uid, event.x, event.y, event.z));

		if (!event.map.isEmpty()) {
			RMap map = resources.getResource("maps", event.map);

			if (map.getEntities().contains(uid)) {
				map.moveEntity(uid, event.x, event.y);
			} else {
				map.addEntity(uid, event.x, event.y);
			}
		}
	}

	@Subscribe
	private void onCreatureChange(UpdateEvent.Creature event) throws ResourceException {
		long uid = event.uid;
		RMap map = resources.getResource("maps", event.map);
		Shape shape = components.getComponent(uid, Shape.class);
		
		if (shape != null) {
			shape.setPosition(event.x, event.y, event.z);
			map.moveEntity(uid, event.x, event.y);
		} else {
			RCreature resource = resources.getResource("creatures", event.id);
			components.putComponent(uid, new Behavior(uid));
			components.putComponent(uid, new Creature.Resource(uid, resource));
			components.putComponent(uid, new Graphics(uid, resource.glyph, resource.color));
			components.putComponent(uid, new Shape(uid, event.x, event.y, event.z));
			map.addEntity(uid, event.x, event.y);
		}
	}
	
	@Subscribe
	private void onCreatureMove(UpdateEvent.Move event) throws ResourceException {
		RMap map = resources.getResource("maps", event.map);
		Shape shape = components.getComponent(event.uid, Shape.class);
		shape.setPosition(event.x, event.y, event.z);			

		if (event.uid != 0) {
			map.moveEntity(event.uid, event.x, event.y);
		}
	}
	
	@Subscribe
	private void onEntityRemove(UpdateEvent.Remove event) throws ResourceException {
		RMap map = resources.getResource("maps", event.map);
		map.removeEntity(event.uid);
	}
	
	/**
	 * Gives a warning when an event is detected that no other object is currently 
	 * listening to.
	 * 
	 * @param event
	 */
	@Subscribe
	private void monitor(DeadEvent event) {
		logger.warning("client received a dead event: " + event.getEvent());
	}

	/**
	 * Configures the file system with the required modules.
	 * 
	 * @param event
	 * @throws FileNotFoundException
	 */
	@Subscribe
	private void configure(ClientConfigurationEvent event) throws FileNotFoundException {
		for(String module : event.getModules()) {
			files.addModule(module);
		}
	}	

}
