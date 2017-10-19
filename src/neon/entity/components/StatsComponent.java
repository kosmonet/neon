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

package neon.entity.components;

import neon.common.resources.RCreature;
import neon.entity.Action;

public class StatsComponent implements Component {
	private final RCreature species;
	private final long uid;
	
	private int AP;	// action points
	
	public StatsComponent(long uid, RCreature species) {
		this.species = species;
		this.uid = uid;
	}
	
	/**
	 * Makes the creature perform an action. This means that the action point
	 * cost of the action is deducted from the action points the creature has
	 * left.
	 * 
	 * @param action
	 */
	public void perform(Action action) {
		AP -= action.points;
	}
	
	/**
	 * Checks whether the creature can perform another action in the current 
	 * turn.
	 * 
	 * @return	{@code true} if the action point total is larger than 0, 
	 * 			{@code false} otherwise
	 */
	public boolean isActive() {
		return AP > 0;
	}
	
	/**
	 * Restores part of the action points.
	 */
	public void rest() {
		AP = Math.min(species.speed, AP + species.speed);
	}
	
	public RCreature getSpecies() {
		return species;
	}

	@Override
	public long getEntity() {
		return uid;
	}
}
