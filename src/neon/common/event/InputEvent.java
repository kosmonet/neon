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

import neon.util.Direction;

/**
 * An event containing client input for the server.
 * 
 * @author mdriesen
 *
 */
public class InputEvent extends NeonEvent {
	public static class Move extends InputEvent {
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
}
