/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

import neon.common.resources.RMap;

/**
 * An event containing updates for the client.
 * 
 * @author mdriesen
 *
 */
public abstract class UpdateEvent extends NeonEvent {
	/**
	 * An event to indicate that a game is started.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Start extends UpdateEvent {
		public final String id;
		public final int x, y, z;

		public Start(String id, int x, int y, int z) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	/**
	 * An event to indicate a change of map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Map extends UpdateEvent {
		private final String map;
		
		public Map(RMap map) {
			this.map = map.id;
		}
		
		public String getMap() {
			return map;
		}
	}
	
	/**
	 * An event to signal a creature update.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Creature extends UpdateEvent {
		public final long uid;
		public final String id, map;
		public final int x, y, z;
		
		public Creature(long uid, String id, String map, int x, int y, int z) {
			this.uid = uid;
			this.id = id;
			this.map = map;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static class Move extends UpdateEvent {
		public final long uid;
		public final int x, y, z;
		public final String map;

		public Move(long uid, String map, int x, int y, int z) {
			this.uid = uid;
			this.x = x;
			this.y = y;
			this.z = z;
			this.map = map;
		}
	}
	
	public static class Item extends UpdateEvent {
		public final long uid;
		public final String id, map;
		public final int x, y, z;
		
		public Item(long uid, String id, String map, int x, int y, int z) {
			this.uid = uid;
			this.id = id;
			this.map = map;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
