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

package neon.common.net;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import neon.common.event.NeonEvent;

/**
 * The server socket receives messages from a client socket and stores them on 
 * a queue for later retrieval. 
 * 
 * @author mdriesen
 *
 */
public class ServerSocket {
	private static final Logger logger = Logger.getGlobal();
	
	private final BlockingQueue<NeonEvent> queue = new LinkedBlockingQueue<>();
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
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Sends a message to the connected client socket.
	 * 
	 * @param message
	 */
	@Subscribe
	public void send(NeonEvent message) {
		if(cs == null) {
			logger.warning("client socket not yet connected to a server socket");			
		} else if(!message.isBlocked()) {
			cs.receive(message);
		}
	}
	
	/**
	 * Posts a newly received message on the event queue.
	 * 
	 * @param event
	 */
	public void receive(NeonEvent message) {
		message.block();
		queue.offer(message);
	}
	
	/**
	 * @return the next event on the queue
	 */
	public NeonEvent getEvent() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			logger.warning("server event queue interrupted");
			// return an empty event instead
			return new NeonEvent(){};
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
