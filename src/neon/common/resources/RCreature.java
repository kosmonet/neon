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
 * A creature resource.
 * 
 * @author mdriesen
 *
 */
public class RCreature extends Resource {
	private final String name;
	private final String character;
	private final Color color;
	private final int speed;
	
	/**
	 * Creates a new creature resource with the given id, name, character and color.
	 * 
	 * @param id
	 * @param type
	 */
	public RCreature(String id, String name, String character, Color color, int speed) {
		super(id, "creature", "creatures");
		this.name = name;
		this.color = color;
		this.character = character;
		this.speed = speed;
	}
	
	/**
	 * @return the name of this creature
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the unicode character representing the creature
	 */
	public String getCharacter() {
		return character;
	}
	
	/**
	 * @return	the color of this creature
	 */
	public Color getColor() {
		return color;
	}
	
	public int getSpeed() {
		return speed;
	}
}
