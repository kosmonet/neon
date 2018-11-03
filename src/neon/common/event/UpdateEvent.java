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
	public static final class Start extends UpdateEvent {}
	
	/**
	 * An event to indicate a change of map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Map extends UpdateEvent {
		public final String map;
		
		public Map(RMap map) {
			this.map = map.id;
		}
	}
	
	public static final class Move extends UpdateEvent {
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
	
	public static final class Remove extends UpdateEvent {
		public final long uid;
		public final String map;
		
		public Remove(long uid, String map) {
			this.uid = uid;
			this.map = map;
		}
	}
	
	public static final class Skill extends UpdateEvent {
		public final long uid;
		public final String skill;
		public final int value;
		
		public Skill(long uid, String skill, int value) {
			this.uid = uid;
			this.skill = skill;
			this.value = value;
		}
	}
	
	public static final class Level extends UpdateEvent {
		public final long uid;
		public final int level;
		
		public Level(long uid, int level) {
			this.uid = uid;
			this.level = level;
		}
	}
	
	public static final class Destroy extends UpdateEvent {
		public final long uid;
		
		public Destroy(long uid) {
			this.uid = uid;
		}
	}
}
