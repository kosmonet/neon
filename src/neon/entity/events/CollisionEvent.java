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

package neon.entity.events;

import neon.common.event.NeonEvent;
import neon.entity.entities.Creature;

public class CollisionEvent extends NeonEvent {
	private final Creature one;
	private final Creature two;
	
	public CollisionEvent(Creature one, Creature two) {
		this.one = one;
		this.two = two;
	}
	
	public Creature getBumper() {
		return one;
	}
	
	public Creature getBumped() {
		return two;
	}
}
