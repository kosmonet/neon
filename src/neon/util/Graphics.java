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

package neon.util;

import javafx.scene.paint.Color;

public class Graphics {
	/**
	 * Converts the given color to a hexadecimal RGB value.
	 * 
	 * @param color
	 * @return
	 */
	public static String getColorString(Color color) {
		return String.format("#%02X%02X%02X", 
				(int) (color.getRed()*255),
	            (int) (color.getGreen()*255),
	            (int) (color.getBlue()*255));
	}
}
