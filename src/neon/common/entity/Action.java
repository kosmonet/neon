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

package neon.common.entity;

/**
 * The different actions a creature can take. Each action has a number of 
 * action points. If a creature takes an action, these points are deducted 
 * from the creature's action points. Creature can only take an action if they
 * have more than zero action points.
 * 
 * @author mdriesen
 *
 */
public enum Action {
	/** Move in a straight line (up, down, left, right). */
	MOVE_STRAIGHT(10), 
	/** Move in a diagonal line. */
	MOVE_DIAGONAL(14), 
	/** Attack another creature. */
	ATTACK(10), 
	/** Pickpocket another creature. */
	PICKPOCKET(10);
	
	/** The amount of action points it takes to perform an action. */
	public final int points;
	
	private Action(int points) {
		this.points = points;
	}
}
