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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.paint.Color;

/**
 * A resource representing a type of terrain on the map.
 * 
 * @author mdriesen
 *
 */
public class RTerrain extends Resource {
	public enum Modifier {
		LIQUID;
	}
	
	public final Color color;
	public final char glyph;
	public final String name;
	
	private final Set<Modifier> modifiers = new HashSet<>();
	
	public RTerrain(String id, String name, char glyph, Color color) {
		super(id, "terrain");
		this.color = color;
		this.glyph = glyph;
		this.name = name;
	}
	
	public void addModifier(Modifier modifier) {
		modifiers.add(modifier);
	}
	
	public boolean hasModifier(Modifier modifier) {
		return modifiers.contains(modifier);
	}
	
	public Set<Modifier> getModifiers() {
		return Collections.unmodifiableSet(modifiers);
	}
}
