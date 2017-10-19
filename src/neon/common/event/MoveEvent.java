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

import java.awt.Point;

import neon.entity.entities.Creature;

/**
 * An event to signal creature movement.
 * 
 * @author mdriesen
 *
 */
public class MoveEvent extends ServerEvent {
	public final Creature creature;
	public final Point position;
	
	protected MoveEvent(Creature creature, Point position) {
		this.creature = creature;
		this.position = position;
	}
	
	/**
	 * A {@code MoveEvent} to signal a request to move a creature to a new
	 * position.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Start extends MoveEvent {
		public Start(Creature creature, Point position) {
			super(creature, position);
		}
	}
	
	/**
	 * A {@code MoveEvent} to signal that a creature was moved to a new
	 * position.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class End extends MoveEvent {
		public End(Creature creature, Point position) {
			super(creature, position);
		}		
	}
}
