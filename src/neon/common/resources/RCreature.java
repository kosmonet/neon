/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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

import java.util.Objects;

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
	/** The strength of the creature. */
	public final int strength;
	/** The constitution of the creature. */
	public final int constitution;
	/** The dexterity of the creature. */
	public final int dexterity;
	/** The intelligence of the creature. */
	public final int intelligence;
	/** The wisdom of the creature. */
	public final int wisdom;
	/** The charisma of the creature. */
	public final int charisma;
	/** A description of the creature. */
	public final String description;
	
	private final int hash;
	
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
		hash = Objects.hash(charisma, color, constitution, description, 
				dexterity, glyph, intelligence, name, speed, strength, wisdom);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + hash;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!super.equals(other)) {
			return false;
		} else if (other instanceof RCreature) {
			RCreature rc = (RCreature) other;
			return charisma == rc.charisma && Objects.equals(color, rc.color) && constitution == rc.constitution
					&& Objects.equals(description, rc.description) && dexterity == rc.dexterity
					&& glyph == rc.glyph && intelligence == rc.intelligence && Objects.equals(name, rc.name)
					&& speed == rc.speed && strength == rc.strength && wisdom == rc.wisdom;
		} else {
			return false;
		}
	}
	
	/**
	 * A builder class for creature resources.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Builder {
		private String id;
		private String name;
		private char glyph;
		private Color color;
		private int speed;
		private int strength, constitution, dexterity, intelligence, wisdom, charisma;
		private String description = "";
		
		/**
		 * Initializes the builder with a resource id. The id must not be null.
		 * 
		 * @param id
		 */
		public Builder(String id) {
			this.id = Objects.requireNonNull(id, "id");
		}
		
		/**
		 * Builds a new creature resource.
		 * 
		 * @return
		 */
		public RCreature build() {
			return new RCreature(this);
		}
		
		/**
		 * Sets the name of the creature resource to be built. The name must 
		 * not be null.
		 * 
		 * @param name
		 * @return
		 */
		public Builder setName(String name) {
			this.name = Objects.requireNonNull(name, "name");
			return this;
		}
		
		/**
		 * Sets the look of the creature resource to be built. The color must 
		 * not be null.
		 * 
		 * @param glyph
		 * @param color
		 * @return
		 */
		public Builder setGraphics(char glyph, Color color) {
			this.glyph = glyph;
			this.color = Objects.requireNonNull(color, "color");
			return this;
		}
		
		/**
		 * Sets the speed of the creature resource to be built.
		 * 
		 * @param speed
		 * @return
		 */
		public Builder setSpeed(int speed) {
			this.speed = speed;
			return this;
		}
		
		/**
		 * Sets a description for the creature resource to be built. The 
		 * description must not be null.
		 * 
		 * @param description
		 * @return
		 */
		public Builder setDescription(String description) {
			this.description = Objects.requireNonNull(description, "description");
			return this;
		}
		
		/**
		 * Sets the main stats of the creature resource to be built.
		 * 
		 * 
		 * @param strength
		 * @param constitution
		 * @param dexterity
		 * @param intelligence
		 * @param wisdom
		 * @param charisma
		 * @return
		 */
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
