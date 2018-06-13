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

package neon.common.event;

/**
 * An event that can be posted on the server or client bus. To prevent an 
 * event from bouncing back and forth through the client and server buses, an
 * event should be blocked when passing through a socket for the first time.
 * 
 * TODO: alle data tussen client en server serialiseren
 * 
 * @author mdriesen
 *
 */
public abstract class NeonEvent {
	private boolean blocked = false;
	
	/**
	 * Blocks this event from passing through a socket.
	 */
	public void block() {
		blocked = true;
	}
	
	/**
	 * Indicates whether this event is blocked from passing through a socket.
	 * 
	 * @return
	 */
	public boolean isBlocked() {
		return blocked;
	}

	/**
	 * An event to request the server to send a list of items in the player's
	 * inventory to the client.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Inventory extends NeonEvent {}
	
	/**
	 * Event to signal the server to pause the game. This means in practice 
	 * that the server switches to turn-based mode.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Pause extends NeonEvent {}

	/**
	 * Event to signal the server to unpause the game. This means in practice 
	 * that the server switches to real-time mode.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Unpause extends NeonEvent {}
}
