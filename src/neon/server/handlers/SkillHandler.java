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

package neon.server.handlers;

import java.util.Objects;

import com.google.common.eventbus.EventBus;

import neon.common.entity.Skill;
import neon.common.entity.components.Skills;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentEvent;
import neon.common.event.UpdateEvent;

/**
 * A handler for all skill-related actions.
 * 
 * @author mdriesen
 *
 */
public final class SkillHandler {
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;

	/**
	 * 
	 * @param bus
	 */
	public SkillHandler(EventBus bus) {
		this.bus = Objects.requireNonNull(bus, "event bus");
	}

	/**
	 * Checks if a skill is high enough to succesfully perform an action.
	 * 
	 * @param skills
	 * @param skill
	 * @param stats
	 * @return
	 */
	public boolean checkSkill(Skills skills, Skill skill, Stats stats) {
		useSkill(skills, skill, stats);
		return skills.getSkill(skill) > 10;
	}
	
	
	/**
	 * Checks if the skill has been used enough to level up.
	 * 
	 * @param skill
	 * @return
	 */
	private void useSkill(Skills skills, Skill skill, Stats stats) {
		skills.setSteps(skill, skills.getSteps(skill) + 1);
		
		// check for skill increase
		if (skills.getSteps(skill) == skill.steps) {
			skills.setSteps(skill, 0);
			skills.setSkill(skill, skills.getSkill(skill) + 1);
			skills.addSkillIncreases(1);
			bus.post(new ComponentEvent(skills));
			bus.post(new UpdateEvent.Skills(skills.getEntity(), Skill.SWIMMING, skills.getSkill(Skill.SWIMMING)));
		}
		
		// check for level up
		if (skills.getEntity() == PLAYER_UID && skills.getSkillIncreases() >= 10) {
			skills.resetSkillIncreases();
			stats.setLevel(stats.getLevel() + 1);
			bus.post(new ComponentEvent(skills));
			bus.post(new ComponentEvent(stats));
			bus.post(new UpdateEvent.Level(skills.getEntity(), stats.getLevel()));
		}
	}
}
