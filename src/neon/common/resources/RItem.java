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

package neon.common.resources;

import javafx.scene.paint.Color;
import neon.entity.Slot;

/**
 * An item resource.
 * 
 * @author mdriesen
 *
 */
public class RItem extends Resource {
	/**
	 * The display name.
	 */
	public final String name;
	
	/**
	 * The UTF-8 character to represent the creature on screen.
	 */
	public final String glyph;
	
	/**
	 * The color to render the character with.
	 */
	public final Color color;
	
	public RItem(Builder builder) {
		super(builder.id, "items");
		name = builder.name;
		glyph = builder.glyph;
		color = builder.color;		
	}
	
	public static class Clothing extends RItem {
		public final Slot slot;
		
		public Clothing(Builder builder) {
			super(builder);
			slot = builder.slot;
		}		
	}
	
	public static class Armor extends Clothing {
		public final int rating;
		
		public Armor(Builder builder) {
			super(builder);
			rating = builder.rating;
		}		
	}
	
	public static class Weapon extends RItem {
		public final Slot slot;
		public final String damage;
		
		public Weapon(Builder builder) {
			super(builder);
			damage = builder.damage;
			slot = builder.slot;
		}		
	}
	
	public static class Builder {
		private final String id;
		private String glyph;
		private Color color;
		private String name;
		private Slot slot;
		private int rating;
		private String damage;
		
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder setName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder setGraphics(String glyph, Color color) {
			this.glyph = glyph;
			this.color = color;
			return this;
		}
		
		public Builder setSlot(Slot slot) {
			this.slot = slot;
			return this;
		}

		public Builder setRating(int rating) {
			this.rating = rating;
			return this;
		}

		public Builder setDamage(String damage) {
			this.damage = damage;
			return this;
		}
	}
}
