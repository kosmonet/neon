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

import com.google.common.eventbus.EventBus;

import neon.common.event.ClientConfigurationEvent;
import neon.common.files.NeonFileSystem;
import neon.common.net.ServerSocket;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
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
	private final EventBus bus = new EventBus("Server Bus");
	private final NeonFileSystem files = new NeonFileSystem();
	private final ResourceManager resources = new ResourceManager(files);
	private final ServerSocket socket;
	private final EntityTracker entities;
	
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
		
		// configure the server (file system and resource manager)
		new ServerLoader(bus).configure(files, resources);
		
		// initialize the entity tracker
		try {
			CServer cs = resources.getResource("config", "server");
			entities = new EntityTracker(resources, cs);
		} catch (ResourceException e) {
			throw new IllegalStateException(e);
		}
		
		// add all the systems and various other stuff to the bus
		MovementService mover = new MovementService();

		bus.register(new GameLoader(resources, entities, bus));
		bus.register(new ScriptHandler(bus));
		bus.register(new InventorySystem(entities, bus));
		bus.register(new TurnSystem(resources, entities, bus));
		bus.register(new AISystem(mover));
		bus.register(new InputSystem(entities, bus, mover));
		
		// send configuration message to the client
		try {
			CGame cg = resources.getResource("config", "game");
			bus.post(new ClientConfigurationEvent(cg));
		} catch (ResourceException e) {
			throw new IllegalStateException(e);
		}		
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
}
