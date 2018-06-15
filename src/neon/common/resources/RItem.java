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
	
	/**
	 * Creates a new item resource with the given id and type.
	 * 
	 * @param id
	 * @param type
	 */
	public RItem(String id, String name, String glyph, Color color) {
		super(id, "items");
		this.name = name;
		this.glyph = glyph;
		this.color = color;
	}
}
