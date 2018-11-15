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

package neon.common.resources;

import javafx.scene.paint.Color;

/**
 * A creature resource.
 * 
 * @author mdriesen
 *
 */
public final class RCreature extends Resource {
	/** The display name. */
	public final String name;
	/** The UTF-8 character to represent the creature on screen. */
	public final char glyph;
	/** The color to render the character with. */
	public final Color color;
	/** The speed stat. */
	public final int speed;
	
	public final int strength, constitution, dexterity, intelligence, wisdom, charisma;
	/** The descripion of the creature. */
	public final String description;
	
	private RCreature(Builder builder) {
		super(builder.id, "creatures");
		name = builder.name;
		color = builder.color;
		glyph = builder.glyph;
		speed = builder.speed;
		strength = builder.strength;
		constitution = builder.constitution;
		dexterity = builder.dexterity;
		intelligence = builder.intelligence;
		wisdom = builder.wisdom;
		charisma = builder.charisma;
		description = builder.description;
	}
	
	public static final class Builder {
		private String id;
		private String name;
		private char glyph;
		private Color color;
		private int speed;
		private int strength, constitution, dexterity, intelligence, wisdom, charisma;
		private String description = "";
		
		public Builder(String id) {
			this.id = id;
		}
		
		public RCreature build() {
			return new RCreature(this);
		}
		
		public Builder setName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder setGraphics(char glyph, Color color) {
			this.glyph = glyph;
			this.color = color;
			return this;
		}
		
		public Builder setSpeed(int speed) {
			this.speed = speed;
			return this;
		}
		
		public Builder setDescription(String description) {
			this.description = description.trim().replaceAll(" +", " ");;
			return this;
		}
		
		public Builder setStats(int strength, int constitution, int dexterity, int intelligence, int wisdom, int charisma) {
			this.strength = strength;
			this.constitution = constitution;
			this.dexterity = dexterity;
			this.intelligence = intelligence;
			this.wisdom = wisdom;
			this.charisma = charisma;		
			return this;
		}
	}
}
