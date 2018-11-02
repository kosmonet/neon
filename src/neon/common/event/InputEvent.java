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

package neon.common.event;

import neon.util.Direction;

/**
 * An event containing client input for the server.
 * 
 * @author mdriesen
 *
 */
public abstract class InputEvent extends NeonEvent {
	/**
	 * Event to request the server to move the player in a certain direction 
	 * on the map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Move extends InputEvent {
		private final Direction direction;
		private final String map;
		
		public Move(Direction direction, String map) {
			this.direction = direction;
			this.map = map;
		}
		
		public Direction getDirection() {
			return direction;
		}
		
		public String getMap() {
			return map;
		}
	}
	
	/**
	 * Event to signal the server to pause the game. This means in practice 
	 * that the server switches to turn-based mode.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Pause extends InputEvent {}

	/**
	 * Event to signal the server to unpause the game. This means in practice 
	 * that the server switches to real-time mode.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Unpause extends InputEvent {}
	
	/**
	 * Event to signal the server to quit.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Quit extends NeonEvent {}
	
	/**
	 * Event to signal the server to save the game.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Save extends NeonEvent {}
}
