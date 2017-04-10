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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import com.google.common.eventbus.Subscribe;
import neon.client.ClientSocket;
import neon.system.event.ClientEvent;
import neon.system.event.ServerEvent;

/**
 * The server socket receives messages from a client socket and stores them on 
 * a queue for later retrieval. 
 * 
 * @author mdriesen
 *
 */
public class ServerSocket {
	private static final Logger logger = Logger.getGlobal();
	
	private final BlockingQueue<ServerEvent> queue = new LinkedBlockingQueue<>();
	private final String name;
	private ClientSocket cs;
	
	/**
	 * Initializes this server socket with the given name.
	 * 
	 * @param name
	 */
	public ServerSocket(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	/**
	 * Sends a message to the connected client socket.
	 * 
	 * @param event
	 */
	@Subscribe
	public void send(ClientEvent event) {
		if(cs != null) {
			cs.receive(event);
		}
	}
	
	/**
	 * Posts a newly received message on the event queue.
	 * 
	 * @param event
	 */
	public void receive(ServerEvent event) {
		queue.offer(event);
	}
	
	/**
	 * @return the next event on the queue
	 */
	ServerEvent getEvent() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			logger.warning("server event queue interrupted");
			return new ServerEvent("");
		}
	}
	
	/**
	 * Connects this server socket to a client socket.
	 * 
	 * @param socket
	 */
	public void connect(ClientSocket socket) {
		if(socket.equals(cs)) {
			logger.warning("socket " + socket + " already connected to server socket");
		} else {
			cs = socket;
			socket.connect(this);			
			logger.info(name + " connecting to " + socket);
		}
	}
}
