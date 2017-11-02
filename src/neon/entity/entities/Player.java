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

package neon.entity.entities;

import neon.common.resources.RCreature;
import neon.entity.components.RecordComponent;

/**
 * An entity representing the player character.
 * 
 * @author mdriesen
 *
 */
public class Player extends Creature {
	public final RecordComponent record;
	
	/**
	 * Initializes the player character. No uid is needed, the player 
	 * character always has uid 0.
	 * 
	 * @param name
	 * @param gender
	 * @param species
	 */
	public Player(String name, String gender, RCreature species) {
		super(0, species);
		record = new RecordComponent(uid, name, gender);
	}
	
	@Override
	public String toString() {
		return "player";
	}
}
