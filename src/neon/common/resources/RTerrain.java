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

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import javafx.scene.paint.Color;

/**
 * A resource representing a type of terrain on the map.
 * 
 * @author mdriesen
 *
 */
public final class RTerrain extends Resource {
	/**
	 * Modifiers that can be applied to a terrain type.
	 * 
	 * @author mdriesen
	 *
	 */
	public enum Modifier {
		/** Terrain type that can be passed by swimming. */
		LIQUID, 
		/** Terrain type that can't be passed. */
		WALL;
	}
	
	/** The color of this terrain type. */
	public final Color color;
	/** The UTF-8 character this terrain is represented with. */
	public final char glyph;
	/** The readable name of this terrain type. */
	public final String name;
	
	private final Set<Modifier> modifiers;
	private final int hash;
	
	/**
	 * Initializes a terrain resource. The color and name must not be null.
	 * 
	 * @param id
	 * @param name
	 * @param glyph
	 * @param color
	 * @param modifiers
	 */
	public RTerrain(String id, String name, char glyph, Color color, Iterable<Modifier> modifiers) {
		super(id, "terrain");
		this.color = Objects.requireNonNull(color, "color");
		this.name = Objects.requireNonNull(name, "name");
		this.glyph = glyph;
		this.modifiers = ImmutableSet.copyOf(modifiers);
		hash = Objects.hash(color, glyph, this.modifiers, name);
	}
	
	/**
	 * Checks whether this terrain type has a certain modifier.
	 * 
	 * @param modifier
	 * @return
	 */
	public boolean hasModifier(Modifier modifier) {
		return modifiers.contains(modifier);
	}
	
	/**
	 * Returns a set of terrain modifiers for this type of terrain.
	 * 
	 * @return	an unmodifiable set of {@code Modifier}s
	 */
	public Set<Modifier> getModifiers() {
		return modifiers;
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
		} else if (other instanceof RTerrain) {
			RTerrain rt = (RTerrain) other;
			return Objects.equals(color, rt.color) && glyph == rt.glyph 
					&& Objects.equals(modifiers, rt.modifiers) && Objects.equals(name, rt.name);
		} else {
			return false;
		}
	}
}
