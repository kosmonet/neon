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

import java.util.EnumMap;

import neon.entity.Skill;

public class Skills implements Component {
	private final long uid;
	private final EnumMap<Skill, Integer> skills = new EnumMap<>(Skill.class);
	private final EnumMap<Skill, Integer> steps = new EnumMap<>(Skill.class);
	
	private int increases = 0;
	
	public Skills(long uid) {
		this.uid = uid;
		for (Skill skill : Skill.values()) {
			skills.put(skill, 5);
			steps.put(skill, 0);
		}
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
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
