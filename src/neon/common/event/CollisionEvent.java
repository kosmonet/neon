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
 * Event to signal a collision between two creatures.
 * 
 * @author mdriesen
 *
 */
public final class CollisionEvent extends NeonEvent {
	/** The uid of the creature that was moving. */
	public final long bumper;
	/** The uid of the creature that was bumped into. */
	public final long bumped;
	
	public CollisionEvent(long bumper, long bumped) {
		this.bumper = bumper;
		this.bumped = bumped;
	}
}
