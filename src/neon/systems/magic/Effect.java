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

package neon.systems.magic;

import javafx.scene.paint.Color;
import neon.common.entity.Skill;

public enum Effect {
	// restoration
	HEAL(1, "heal", '♥', Color.RED, Skill.RESTORATION),
	RESTORE_STAMINA(1, "restore stamina", '♉', Color.RED, Skill.RESTORATION),
	RESTORE_MANA(1, "restore mana", '✳', Color.BLUE, Skill.RESTORATION),
	CURE_POISON(1, "cure poison", '⚕', Color.GREEN, Skill.RESTORATION),
	CURE_DISEASE(1, "cure disease", '⚕', Color.GREEN, Skill.RESTORATION),
	
	FORTIFY_STRENGTH(1, "fortify strength", '✳', Color.ORANGERED, Skill.RESTORATION),
	FORTIFY_CONSTITUTION(1, "fortify constitution", '✳', Color.ORANGERED, Skill.RESTORATION),
	FORTIFY_DEXTERITY(1, "fortify dexterity", '✳', Color.ORANGERED, Skill.RESTORATION),
	FORTIFY_INTELLIGENCE(1, "fortify intelligence", '✳', Color.ORANGERED, Skill.RESTORATION),
	FORTIFY_WISDOM(1, "fortify wisdom", '✳', Color.ORANGERED, Skill.RESTORATION),
	FORTIFY_CHARISMA(1, "fortify charisma", '✳', Color.ORANGERED, Skill.RESTORATION),

	RESTORE_STRENGTH(1, "restore strength", '✳', Color.ORANGERED, Skill.RESTORATION),
	RESTORE_CONSTITUTION(1, "restore constitution", '✳', Color.ORANGERED, Skill.RESTORATION),
	RESTORE_DEXTERITY(1, "restore dexterity", '✳', Color.ORANGERED, Skill.RESTORATION),
	RESTORE_INTELLIGENCE(1, "restore intelligence", '✳', Color.ORANGERED, Skill.RESTORATION),
	RESTORE_WISDOM(1, "restore wisdom", '✳', Color.ORANGERED, Skill.RESTORATION),
	RESTORE_CHARISMA(1, "restore charisma", '✳', Color.ORANGERED, Skill.RESTORATION),

	// destruction
	FREEZE(1, "freeze", '❄', Color.LIGHTSKYBLUE, Skill.DESTRUCTION), 
	BURN(1, "burn", '✷', Color.ORANGERED, Skill.DESTRUCTION),
	SHOCK(1, "shock", '⌁', Color.YELLOW, Skill.DESTRUCTION),
	
	DRAIN_STAMINA(1, "drain stamina", '♉', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_HEALTH(1, "drain health", '♥', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_MANA(1, "drain mana", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	
	DRAIN_STRENGTH(1, "drain strength", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_CONSTITUTION(1, "drain constitution", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_DEXTERITY(1, "drain dexterity", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_INTELLIGENCE(1, "drain intelligence", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_WISDOM(1, "drain wisdom", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	DRAIN_CHARISMA(1, "drain charisma", '✳', Color.ORANGERED, Skill.DESTRUCTION),
	
	//alteration
	SHIELD(1, "shield", '⨀', Color.WHITE, Skill.ALTERATION),
	LEVITATE(1, "levitate", 'l', Color.WHITE, Skill.ALTERATION),
	
	RESIST_COLD(1, "resist cold", '❄', Color.WHITE, Skill.ALTERATION),
	RESIST_FIRE(1, "resist fire", '✷', Color.WHITE, Skill.ALTERATION),
	RESIST_SHOCK(1, "resist shock", '⌁', Color.WHITE, Skill.ALTERATION),
	
	// illusion
	INVISIBILITY(1, "invisibility", 'i', Color.WHITE, Skill.ILLUSION),
	PARALYZE(1, "paralyze", 'p', Color.WHITE, Skill.ILLUSION);
	
	
	public final int cost;
	public final String name;
	public final char symbol;
	public final Color color;
	public final Skill skill;
	
	private Effect(int cost, String name, char symbol, Color color, Skill skill) {
		this.cost = cost;
		this.name = name;
		this.symbol = symbol;
		this.color = color;
		this.skill = skill;
	}
}
