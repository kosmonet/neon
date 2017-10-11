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

package neon.common.graphics;

import java.util.HashMap;
import javafx.scene.canvas.Canvas;

public interface EntityRenderer {
	/**
	 * Draws the entities on the map.
	 * 
	 * @param entity
	 * @param xmin
	 * @param ymin
	 * @param scale
	 */	
	public void drawEntity(Object entity, int xmin, int ymin, int scale);
	
	/**
	 * Sets the JavaFX {@code Canvas}ses to be used for drawing the entities.
	 * 
	 * @param layers
	 */
	public void setLayers(HashMap<Integer, Canvas> layers);
}
