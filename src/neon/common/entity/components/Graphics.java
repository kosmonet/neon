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

package neon.common.entity.components;

import java.util.Objects;

import javafx.scene.paint.Color;

/**
 * 
 * @author mdriesen
 *
 */
public final class Graphics implements Component {
	private final long uid;
	private final char glyph;
	private final Color color;
	
	public Graphics(long uid, char glyph, Color color) {
		this.uid = uid;
		this.glyph = glyph;
		this.color = Objects.requireNonNull(color, "color");
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Graphics:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	/**
	 * Returns the color an entity should be rendered with.
	 * 
	 * @return	a {@code Color}
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the glyph an entity should be rendered with.
	 * 
	 * @return
	 */
	public char getGlyph() {
		return glyph;
	}
}
