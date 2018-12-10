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

package neon.common.entity.components;

import java.util.Objects;

import neon.server.entity.Map;

/**
 * A component that indicates a task that should be performed by one of the
 * systems on an entity with this component.
 * 
 * @author mdriesen
 *
 */
public abstract class Task implements Component {
	private final long uid;
	
	public Task(long uid) {
		this.uid = uid;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public static final class Action extends Task {
		public final int fraction;
		
		public Action(long uid, int fraction) {
			super(uid);
			this.fraction = fraction;
		}
	}
	
	public static final class Think extends Task {
		public Think(long uid) {
			super(uid);
		}
	}
	
	public static final class Move extends Task {
		public final int x, y;
		private final Map map;
		
		public Move(long uid, int x, int y, Map map) {
			super(uid);
			this.x = x;
			this.y = y;
			this.map = Objects.requireNonNull(map, "map");
		}
		
		public Map getMap() {
			return map;
		}
	}
}
