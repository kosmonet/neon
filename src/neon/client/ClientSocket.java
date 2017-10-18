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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import neon.common.event.ClientEvent;
import neon.common.event.ServerEvent;
import neon.server.ServerSocket;

/**
 * The client socket receives message from a connected server socket and posts
 * them on a queue for later retrieval.
 * 
 * @author mdriesen
 *
 */
public class ClientSocket {
	private static final Logger logger = Logger.getGlobal();

	private final BlockingQueue<ClientEvent> queue = new LinkedBlockingQueue<>();
	private final String name;
	private ServerSocket ss;

	/**
	 * Initializes this client socket with the given name.
	 * 
	 * @param name
	 */
	public ClientSocket(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Sends a message to the connected server socket.
	 * 
	 * @param message
	 */
	@Subscribe
	public void send(ServerEvent message) {
		if(ss != null) {
			ss.receive(message);
		} else {
			logger.warning("client socket not yet connected to a server socket");
		}
	}
	
	/**
	 * Posts a received message on the queue.
	 * 
	 * @param message
	 */
	public void receive(ClientEvent message) {
		queue.offer(message);
	}
	
	/**
	 * @return the next event on the queue
	 */
	ClientEvent getEvent() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			logger.warning("client event queue interrupted");
			// return an empty event instead
			return new ClientEvent();
		}
	}
	
	/**
	 * Connects this client socket with a server socket.
	 * 
	 * @param socket
	 */
	public void connect(ServerSocket socket) {
		if(socket.equals(ss)) {
			logger.warning("socket " + socket + " already connected to client socket");			
		} else {
			ss = socket;
		}
	}
}
