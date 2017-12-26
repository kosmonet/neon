/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.stage.Stage;
import neon.client.modules.ConversationModule;
import neon.client.modules.GameModule;
import neon.client.modules.InventoryModule;
import neon.client.modules.LoadModule;
import neon.client.modules.MainMenuModule;
import neon.client.modules.MapModule;
import neon.client.modules.Module;
import neon.client.modules.NewGameModule;
import neon.client.modules.Transition;
import neon.client.modules.TransitionEvent;
import neon.common.event.NeonEvent;
import neon.common.event.QuitEvent;
import neon.common.net.ClientSocket;

public class Client implements Runnable {
	private static final Logger logger = Logger.getGlobal();

	private final EventBus bus = new EventBus("Client Bus");
	private final ArrayList<Module> modules = new ArrayList<>();
	private final ClientSocket socket;
	private final UserInterface ui;
	
	/**
	 * Initializes the client.
	 * 
	 * @param version	the current version of the client
	 * @param socket	the socket used for communication with the server
	 * @param stage		the JavaFX stage used for drawing the user interface
	 */
	public Client(String version, ClientSocket socket, Stage stage) {
		this.socket = socket;
		bus.register(socket);
		bus.register(new BusListener());
		ui = new UserInterface(stage);
		bus.register(ui);
		
		// server should cleanly shut down if client is closed
		stage.setOnCloseRequest(event -> bus.post(new QuitEvent()));
		
		// initialize all modules and enter the first one
		initModules(version);
		modules.get(0).setActive(true);
		modules.get(0).enter(new TransitionEvent("start"));
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
		ClientProvider provider = new ClientProvider();
		
		// client uses the first module in the list as the start state
		MainMenuModule mainMenu = new MainMenuModule(ui, version, bus);
		modules.add(mainMenu);
		bus.register(mainMenu);

		NewGameModule newGame = new NewGameModule(ui, bus);
		modules.add(newGame);
		bus.register(newGame);
		
		LoadModule loadGame = new LoadModule(ui, bus);
		modules.add(loadGame);
		bus.register(loadGame);
		
		GameModule game = new GameModule(ui, bus, provider);
		modules.add(game);
		bus.register(game);
		
		InventoryModule inventory = new InventoryModule(ui, bus, provider);
		modules.add(inventory);
		bus.register(inventory);
		
		MapModule map = new MapModule(ui, bus, provider);
		modules.add(map);
		bus.register(map);
		
		ConversationModule conversation = new ConversationModule(ui, bus);
		modules.add(conversation);
		bus.register(conversation);
		
		// register all state transitions on the bus to listen for transition events
		bus.register(new Transition(mainMenu, newGame, "new game"));
		bus.register(new Transition(newGame, mainMenu, "cancel"));
		bus.register(new Transition(newGame, game, "start game"));
		
		bus.register(new Transition(mainMenu, loadGame, "load game"));
		bus.register(new Transition(loadGame, mainMenu, "cancel"));
		bus.register(new Transition(loadGame, game, "start game"));
		
		bus.register(new Transition(game, inventory, "inventory"));
		bus.register(new Transition(inventory, game, "cancel"));

		bus.register(new Transition(game, map, "map"));
		bus.register(new Transition(map, game, "cancel"));
		
		bus.register(new Transition(game, conversation, "talk"));
		bus.register(new Transition(conversation, game, "cancel"));
	}
	
	private class BusListener {
		@Subscribe
		private void monitor(DeadEvent event) {
			logger.warning("client received a dead event: " + event.getEvent());
		}
	}
}
