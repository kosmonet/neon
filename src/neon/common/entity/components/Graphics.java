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

package neon.common.entity.components;

import javafx.scene.paint.Color;

public class Graphics implements Component {
	private final long uid;
	private final String glyph;
	private final Color color;
	
	public Graphics(long uid, String glyph, Color color) {
		this.uid = uid;
		this.glyph = glyph;
		this.color = color;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public Color getColor() {
		return color;
	}

	public String getGlyph() {
		return glyph;
	}
}
