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

import java.util.EnumMap;
import java.util.Map;

import neon.common.entity.Skill;

/**
 * The set of skills a creature can have.
 * 
 * @author mdriesen
 *
 */
public final class Skills implements Component {
	private final long uid;
	private final Map<Skill, Integer> skills = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> steps = new EnumMap<>(Skill.class);
	
	private int increases = 0;
	
	/**
	 * Initializes this skill component.
	 * 
	 * @param uid	the uid of the creature these skills belong to
	 */
	public Skills(long uid) {
		this.uid = uid;
		for (Skill skill : Skill.values()) {
			skills.put(skill, 5);
			steps.put(skill, 0);
		}
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Skills:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	/**
	 * Sets the value of a skill.
	 * 
	 * @param skill
	 * @param value
	 */
	public void setSkill(Skill skill, int value) {
		skills.put(skill, value);
	}
	
	public int getSkill(Skill skill) {
		return skills.get(skill);
	}
	
	public void setSteps(Skill skill, int value) {
		steps.put(skill, value);
	}
	
	public int getSteps(Skill skill) {
		return steps.get(skill);
	}
	
	/**
	 * Returns the amount of skill increases a creature has experienced since
	 * the last reset.
	 * 
	 * @return
	 */
	public int getSkillIncreases() {
		return increases;
	}

	public void resetSkillIncreases() {
		increases = 0;
	}
	
	public void addSkillIncreases(int amount) {
		increases += amount;
	}
}
