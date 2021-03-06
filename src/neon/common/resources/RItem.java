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
import java.util.Optional;

import javafx.scene.paint.Color;
import neon.common.entity.ArmorType;
import neon.systems.magic.Effect;

/**
 * An item resource.
 * 
 * @author mdriesen
 *
 */
public class RItem extends Resource {
	private static final int prime = 31;
	
	/** The display name. */
	public final String name;
	/** The UTF-8 character to represent the item on screen. */
	public final char glyph;
	/** The color to render the character with. */
	public final Color color;
	/** The price of this item. */
	public final int price;
	/** The weight of this item. */
	public final int weight;
	
	private final int hash;
	
	private RItem(Builder builder) {
		super(builder.id, "items");
		name = builder.name;
		glyph = builder.glyph;
		color = builder.color;
		price = builder.price;
		weight = builder.weight;
		hash = Objects.hash(color, glyph, name, price, weight);
	}

	@Override
	public int hashCode() {
		return prime * super.hashCode() + hash;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!super.equals(other)) {
			return false;
		} else if (other instanceof RItem) {
			RItem ri = (RItem) other;
			return Objects.equals(color, ri.color) && glyph == ri.glyph && Objects.equals(name, ri.name)
					&& price == ri.price && weight == ri.weight;
		} else {
			return false;
		}
	}
	
	/**
	 * A coin resource. Coins aren't meant to be added to an inventory
	 * separately, they should disappear when picked up.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Coin extends RItem {
		public Coin(Builder builder) {
			super(builder);
		}
	}
	
	/**
	 * A container resource. Containers aren't meant to be picked up or 
	 * carried in an inventory. They should stay fixed in one place on the map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Container extends RItem {
		public Container(Builder builder) {
			super(builder);
		}
	}
	
	/**
	 * A clothing resource. Clothing can be worn on one or more body slots. 
	 * Clohting can have an optional enchantment.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Clothing extends RItem {
		/** The body slot this piece of clothing is worn on. */
		public final Slot slot;
		/** The magic effect of the enchantment. */
		public final Optional<Effect> effect;
		/** The magnitude of the enchantment. */
		public final int magnitude;
		
		private final int hash;
		
		public Clothing(Builder builder) {
			super(builder);
			slot = builder.slot;
			magnitude = builder.magnitude;
			effect = Optional.ofNullable(builder.effect);
			hash = Objects.hash(effect, magnitude, slot);
		}

		@Override
		public int hashCode() {
			return prime * super.hashCode() + hash;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (!super.equals(other)) {
				return false;
			} else if (other instanceof Clothing) {
				Clothing c = (Clothing) other;
				return Objects.equals(effect, c.effect) && magnitude == c.magnitude && slot == c.slot;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * An armor resource. Armor functions like clothing, but with an armor 
	 * rating.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Armor extends Clothing {
		/** The armor rating. */
		public final int rating;
		/** The weight class. */
		public final ArmorType type;
		
		private final int hash;
		
		public Armor(Builder builder) {
			super(builder);
			rating = builder.rating;
			type = builder.type;
			hash = Objects.hash(rating, type);
		}

		@Override
		public int hashCode() {
			return prime * super.hashCode() + hash;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (!super.equals(other)) {
				return false;
			} else if (other instanceof Armor) {
				Armor a = (Armor) other;
				return rating == a.rating && type == a.type;
			} else {
				return false;
			}		
		}
	}
	
	/**
	 * A weapon resource. Weapons do damage.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Weapon extends RItem {
		/** The damage the weapon does. */
		public final String damage;
		
		public Weapon(Builder builder) {
			super(builder);
			damage = builder.damage;
		}

		@Override
		public int hashCode() {
			return prime * super.hashCode() + Objects.hash(damage);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (!super.equals(other)) {
				return false;
			} else if (other instanceof Weapon) {
				Weapon w = (Weapon) other;
				return Objects.equals(damage, w.damage);
			} else {
				return false;
			}
		}		
	}
	
	public static final class Potion extends RItem {
		/** The magic effect of the enchantment. */
		public final Optional<Effect> effect;
		/** The magnitude of the enchantment. */
		public final int magnitude;
		
		private final int hash;

		public Potion(Builder builder) {
			super(builder);
			magnitude = builder.magnitude;
			effect = Optional.ofNullable(builder.effect);
			hash = Objects.hash(effect, magnitude);
		}

		@Override
		public int hashCode() {
			return prime * super.hashCode() + hash;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (!super.equals(other)) {
				return false;
			} else if (other instanceof Potion) {
				Potion p = (Potion) other;
				return Objects.equals(effect, p.effect) && magnitude == p.magnitude;
			} else {
				return false;
			}
		}
	}
	
	public static final class Door extends RItem {
		/** The character to represent a door in opened condition. */
		public final char opened;
		/** The character to represent a door in closed condition. */
		public final char closed;
		
		private final int hash;

		public Door(Builder builder) {
			super(builder);
			opened = builder.glyph;
			closed = builder.closed;
			hash = Objects.hash(closed, opened);
		}

		@Override
		public int hashCode() {
			return prime * super.hashCode() + hash;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (!super.equals(other)) {
				return false;
			} else if (other instanceof Door) {
				Door d = (Door) other;
				return closed == d.closed && opened == d.opened;
			} else {
				return false;
			}
		}		
	}

	/**
	 * A builder for the {@code RItem} class.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Builder {
		private final String id;
		private char glyph;
		private Color color;
		private String name;
		private Slot slot;
		private int rating;
		private String damage;
		private Effect effect;
		private int magnitude;
		private int price;
		private int weight;
		private ArmorType type;
		private char closed;
		
		/**
		 * Initialize the builder with an id and a name.
		 * 
		 * @param id
		 * @param name
		 */
		public Builder(String id, String name) {
			this.id = Objects.requireNonNull(id, "id");
			this.name = Objects.requireNonNull(name, "name");
		}
		
		/**
		 * Builds a new item resource.
		 * 
		 * @return
		 */
		public RItem build() {
			return new RItem(this);
		}
		
		/**
		 * Sets the glyph and the color.
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
		
		public Builder setSlot(Slot slot) {
			this.slot = Objects.requireNonNull(slot, "slot");
			return this;
		}

		public Builder setRating(int rating) {
			this.rating = rating;
			return this;
		}

		public Builder setArmorType(ArmorType type) {
			this.type = Objects.requireNonNull(type, "armor type");
			return this;
		}

		public Builder setDamage(String damage) {
			this.damage = Objects.requireNonNull(damage, "damage");
			return this;
		}
		
		public Builder setMagic(Effect effect, int magnitude) {
			this.effect = effect;
			this.magnitude = magnitude;
			return this;
		}
		
		public Builder setPrice(int price) {
			this.price = price;
			return this;
		}
		
		public Builder setWeight(int weight) {
			this.weight = weight;
			return this;
		}
		
		public Builder setClosed(char closed) {
			this.closed = closed;
			return this;
		}
	}
}
