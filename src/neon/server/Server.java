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

package neon.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;

import neon.common.event.ClientConfigurationEvent;
import neon.common.event.QuitEvent;
import neon.common.event.TimerEvent;
import neon.common.files.NeonFileSystem;
import neon.common.net.ServerSocket;
import neon.common.resources.CClient;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.MovementService;
import neon.entity.systems.AISystem;
import neon.entity.systems.InputSystem;
import neon.entity.systems.InventorySystem;
import neon.entity.systems.TurnSystem;

/**
 * The server part of the neon engine.
 * 
 * @author mdriesen
 *
 */
public class Server implements Runnable {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus = new EventBus("Server Bus");
	private final NeonFileSystem files = new NeonFileSystem();
	private final ResourceManager resources = new ResourceManager(files);
	private final ServerSocket socket;
	private final EntityTracker entities = new EntityTracker(files, resources);
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	/**
	 * Initializes the server.
	 * 
	 * @param version	the current version of the engine
	 * @param socket	the socket used to communicate with a client
	 */
	public Server(String version, ServerSocket socket) {
		// initialize communication with the client
		this.socket = socket;
		bus.register(socket);
		bus.register(this);
		
		// configure the server (file system, resource manager and entity tracker)
		new ServerLoader(bus).configure(files, resources, entities);
		
		// add all the systems and various other stuff to the bus
		MovementService mover = new MovementService();

		bus.register(new GameLoader(files, resources, entities, bus));
		bus.register(new ScriptHandler(bus));
		bus.register(new InventorySystem(entities, bus));
		bus.register(new TurnSystem(resources, entities, bus));
		bus.register(new AISystem(mover));
		bus.register(new InputSystem(entities, bus, mover));
		
		// send configuration message to the client
		try {
			CClient cc = resources.getResource("config", "client");
			bus.post(new ClientConfigurationEvent(cc));
		} catch (ResourceException e) {
			throw new IllegalStateException("Could not load client configuration", e);
		}
		
		// start the timer for the game loop
		executor.scheduleAtFixedRate(() -> socket.receive(new TimerEvent()), 500, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Continuously tries to receive events from the event queue and post them 
	 * on the server event bus.
	 */
	public void run() {
		while(true) {
			bus.post(socket.getEvent());
		}
	}
	
	/**
	 * Exits the application in an orderly fashion.
	 * 
	 * @param event
	 */
	@Subscribe
	private void quitGame(QuitEvent event) {
		logger.info("quit game");
		executor.shutdown();
		Platform.exit();
	}
}
