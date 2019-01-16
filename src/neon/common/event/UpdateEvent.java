/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018-2019 - Maarten Driesen
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

import java.util.Objects;

import neon.common.entity.Skill;

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
	public static final class Start extends UpdateEvent {
		public final int time;
		
		public Start() {
			this(0);
		}
		
		public Start(int time) {
			this.time = time;
		}
	}
	
	/**
	 * An event to indicate a change of map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Map extends UpdateEvent {
		public final int uid;
		public final String id;
		
		public Map(int uid, String id) {
			this.uid = uid;
			this.id = Objects.requireNonNull(id, "map id");
		}
	}
	
	/**
	 * An event to signal a moved entity.
	 * 
	 * @author mdriesen
	 */
	public static final class Move extends UpdateEvent {
		public final long uid;
		public final int x, y, z;
		public final int map;

		public Move(long uid, int map, int x, int y, int z) {
			this.uid = uid;
			this.x = x;
			this.y = y;
			this.z = z;
			this.map = map;
		}
	}
	
	/**
	 * An event to indicate that an entity is removed from a map (e.g. when 
	 * picking up an item).
	 * 
	 * @author mdriesen
	 */
	public static final class Remove extends UpdateEvent {
		public final long uid;
		public final int map;
		
		public Remove(long uid, int map) {
			this.uid = uid;
			this.map = map;
		}
	}
	
	/**
	 * An event to indicate skill increases.
	 * 
	 * @author mdriesen
	 */
	public static final class Skills extends UpdateEvent {
		public final long uid;
		public final Skill skill;
		public final int value;
		
		public Skills(long uid, Skill skill, int value) {
			this.uid = uid;
			this.skill = Objects.requireNonNull(skill, "skill");
			this.value = value;
		}
	}
	
	/**
	 * An event to indicate level increases.
	 * 
	 * @author mdriesen
	 */
	public static final class Level extends UpdateEvent {
		public final long uid;
		public final int level;
		
		public Level(long uid, int level) {
			this.uid = uid;
			this.level = level;
		}
	}
	
	/**
	 * An event to indicate that an entity is destroyed and should be removed
	 * from the game.
	 * 
	 * @author mdriesen
	 */
	public static final class Destroy extends UpdateEvent {
		public final long uid;
		
		public Destroy(long uid) {
			this.uid = uid;
		}
	}
}
