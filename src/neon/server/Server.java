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

import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import neon.system.event.ClientConfigurationEvent;
import neon.system.files.NeonFileSystem;
import neon.system.resources.CGame;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

/**
 * The server part of the neon engine.
 * 
 * @author mdriesen
 *
 */
public class Server implements Runnable {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus = new EventBus("Server Bus");
	private final ServerSocket socket;
	private final NeonFileSystem files;
	private final ResourceManager resources;
	
	/**
	 * Initializes the server.
	 * 
	 * @param version	the current version of the server
	 * @param socket	the socket used to communicate with a client
	 */
	public Server(String version, ServerSocket socket) {
		this.socket = socket;
		bus.register(socket);	
		files = new NeonFileSystem();
		resources = new ResourceManager(files);
		new ServerLoader().configure(files, resources);
		
		// send configuration message to the client
		try {
			CGame cg = resources.getResource("config", "game");
			bus.post(new ClientConfigurationEvent(cg));
		} catch (ResourceException e) {
			logger.severe(e.getMessage());
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
