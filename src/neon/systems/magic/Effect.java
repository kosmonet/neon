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
	
	// destruction
	FREEZE(1, "freeze", '❄', Color.LIGHTSKYBLUE, Skill.DESTRUCTION), 
	BURN(1, "burn", '✷', Color.ORANGERED, Skill.DESTRUCTION);
	
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
