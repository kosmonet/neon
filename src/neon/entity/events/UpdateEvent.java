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

package neon.entity.events;

import neon.common.event.NeonEvent;
import neon.common.resources.RMap;
import neon.entity.entities.Player;

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

		public Start(Player player) {
			id = player.info.getResource().id;
			x = player.shape.getX();
			y = player.shape.getY();
			z = player.shape.getZ();
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
		public final String id, mapID;
		public final int x, y, z;
		
		public Creature(long uid, String id, String mapID, int x, int y, int z) {
			this.uid = uid;
			this.id = id;
			this.mapID = mapID;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static class Move extends UpdateEvent {
		public final long uid;
		public final int x, y, z;

		public Move(long uid, int x, int y, int z) {
			this.uid = uid;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static class Item extends UpdateEvent {
		public final long uid;
		public final String id;
		
		public Item(long uid, String id) {
			this.uid = uid;
			this.id = id;
		}
	}
}
