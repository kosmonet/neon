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

package neon.common.entity.components;

import neon.common.entity.Action;
import neon.common.resources.RCreature;

/**
 * A component that keeps track of all creature stats.
 * 
 * @author mdriesen
 *
 */
public final class Stats implements Component {
	private final long uid;
	private final int speed;
	
	private double AP;	// action points
	private int level = 1;
	private int healthMod = 0;
	private int manaMod = 0;
	
	private int str = 10, con = 10, dex = 10, wis = 10, іnt = 10, cha = 10;
	
	public Stats(long uid, RCreature species) {
		this.uid = uid;
		this.speed = species.speed;
		AP = species.speed;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Stats:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
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
		AP = Math.min(speed, AP + speed);
	}
	
	/**
	 * Restores a fraction of the action points. A fraction of e.g. 5 means 
	 * that 1/5 of the action points are restored.
	 * 
	 * @param fraction
	 */
	public void restoreAP(int fraction) {
		AP = Math.min(speed, AP + speed/fraction);
	}
	
	/**
	 * Returns the base intelligence, without any modifiers applied.
	 * 
	 * @return	the base intelligence
	 */
	public int getBaseInt() {
		return іnt;
	}
	
	public int getBaseStr() {
		return str;
	}
	
	public int getBaseCon() {
		return con;
	}
	
	public int getBaseDex() {
		return dex;
	}
	
	public int getBaseWis() {
		return wis;
	}
	
	public int getBaseCha() {
		return cha;
	}
	
	public void setBaseInt(int intelligence) {
		іnt = intelligence;
	}
	
	public void setBaseStr(int strength) {
		str = strength;
	}
	
	public void setBaseCon(int constitution) {
		con = constitution;
	}
	
	public void setBaseDex(int dexterity) {
		dex = dexterity;
	}
	
	public void setBaseWis(int wisdom) {
		wis = wisdom;
	}
	
	public void setBaseCha(int charisma) {
		cha = charisma;
	}
	
	public int getBaseHealth() {
		return 3*con + level*con/3;
	}
	
	public int getHealth() {
		return getBaseHealth() + healthMod;
	}
	
	public void addHealth(int amount) {
		healthMod += amount;
	}
	
	public int getBaseMana() {
		return іnt*6;
	}
	
	public int getMana() {
		return getBaseMana() + manaMod;
	}
	
	public void addMana(int amount) {
		manaMod += amount;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
}
