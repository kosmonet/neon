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

package neon.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;

import neon.common.event.ConfigurationEvent;
import neon.common.event.ClientEvent;
import neon.common.event.InputEvent;
import neon.common.event.TimerEvent;
import neon.common.files.NeonFileSystem;
import neon.common.net.ServerSocket;
import neon.common.resources.CClient;
import neon.common.resources.CServer;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntitySaver;
import neon.server.entity.EntityManager;
import neon.server.handlers.GameLoader;
import neon.server.handlers.InventoryHandler;
import neon.server.handlers.SleepHandler;
import neon.server.handlers.StealthHandler;
import neon.server.systems.SystemManager;
import neon.systems.conversation.ConversationSystem;
import neon.systems.magic.MagicSystem;
import neon.systems.narrative.QuestSystem;
import neon.systems.scripting.ScriptHandler;
import neon.systems.time.TimeSystem;

/**
 * The server part of the neon engine.
 * 
 * @author mdriesen
 *
 */
public final class Server implements Runnable {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus = new EventBus("Server Bus");
	private final NeonFileSystem files = new NeonFileSystem();
	private final ResourceManager resources = new ResourceManager();
	private final ServerSocket socket;
	private final EntityManager entities = new EntityManager(files, new EntitySaver(resources));
	private final Configuration config = new Configuration();
	private final SystemManager systems = new SystemManager(resources, entities, bus, config);
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
		ScriptHandler scripting = new ScriptHandler(bus);
		bus.register(scripting);
		bus.register(new GameLoader(files, resources, entities, bus));
		bus.register(new InventoryHandler(entities, bus, config));
		bus.register(new ConversationSystem(files, resources, entities, bus));
		bus.register(new StealthHandler(entities, bus));
		bus.register(new SleepHandler(entities, bus));
		bus.register(new MagicSystem(files, resources, entities, bus));
		bus.register(new TimeSystem(config, scripting));
		bus.register(new QuestSystem(files, resources));
		bus.register(systems);
		
		// send configuration message to the client
		try {
			CClient cc = resources.getResource("config", "client");
			CServer cs = resources.getResource("config", "server");
			bus.post(new ConfigurationEvent(cc, cs));
		} catch (ResourceException e) {
			throw new IllegalStateException("Could not load client configuration.", e);
		}
		
		// start the timer for the game loop
		executor.scheduleAtFixedRate(() -> socket.receive(new TimerEvent()), 500, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Continuously tries to receive events from the event queue and post them 
	 * on the server event bus.
	 */
	public void run() {
		while (true) {
			bus.post(socket.getEvent());
		}
	}
	
	/**
	 * Gives a warning when an event is detected that no other object is currently 
	 * listening to.
	 * 
	 * @param event
	 */
	@Subscribe
	private void monitor(DeadEvent event) {
		logger.warning("server received a dead event: " + event.getEvent());
	}
	
	/**
	 * Throws an error when a client event has somehow made its way into the
	 * server.
	 * 
	 * @param event
	 */
	@Subscribe
	private void monitor(ClientEvent event) {
		throw new AssertionError("Server received a client event!");
	}
	
	/**
	 * Exits the application in an orderly fashion.
	 * 
	 * @param event
	 */
	@Subscribe
	private void quitGame(InputEvent.Quit event) {
		logger.info("quit game");
		executor.shutdown();
		Platform.exit();
	}
}
