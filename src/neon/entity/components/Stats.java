/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

public class Stats implements Component {
	private final RCreature species;
	private final long uid;
	
	private double AP;	// action points
	
	private int strength = 10, constitution = 10, dexterity = 10, wisdom = 10, intelligence = 10, charisma = 10;
	
	public Stats(long uid, RCreature species) {
		this.species = species;
		this.uid = uid;
		AP = species.speed;
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
	 * Restores a part of the action points equal to the creature's speed.
	 */
	public void restoreAP() {
		AP = Math.min(species.speed, AP + species.speed);
	}
	
	/**
	 * Restores a fraction of the action points. A fraction of e.g. 5 means 
	 * that 1/5 of the action points are restored.
	 * 
	 * @param fraction
	 */
	public void restoreAP(int fraction) {
		AP = Math.min(species.speed, AP + species.speed/fraction);
	}
	
	public int getBaseInt() {
		return intelligence;
	}
	
	public int getBaseStr() {
		return strength;
	}
	
	public int getBaseCon() {
		return constitution;
	}
	
	public int getBaseDex() {
		return dexterity;
	}
	
	public int getBaseWis() {
		return wisdom;
	}
	
	public int getBaseCha() {
		return charisma;
	}
	
	public void setBaseInt(int intelligence) {
		this.intelligence = intelligence;
	}
	
	public void setBaseStr(int strength) {
		this.strength = strength;
	}
	
	public void setBaseCon(int constitution) {
		this.constitution = constitution;
	}
	
	public void setBaseDex(int dexterity) {
		this.dexterity = dexterity;
	}
	
	public void setBaseWis(int wisdom) {
		this.wisdom = wisdom;
	}
	
	public void setBaseCha(int charisma) {
		this.charisma = charisma;
	}
	
	public RCreature getSpecies() {
		return species;
	}

	@Override
	public long getEntity() {
		return uid;
	}
}
