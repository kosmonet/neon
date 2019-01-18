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

import java.util.Comparator;
import java.util.Map;

import javafx.scene.canvas.Canvas;

/**
 * A class to render entities on JavaFX {@code Canvas}ses.
 * 
 * @author mdriesen
 * @param <T>
 */
public interface EntityRenderer<T> {
	/**
	 * Draws the entities on the map. The interpretation of the scaling factor
	 * may depend on the implementation. 
	 * 
	 * @param entity	an object to render
	 * @param xmin	the leftmost position visible on the screen
	 * @param ymin	the topmost position visible on the screen
	 * @param scale	a scaling factor to apply to the rendering
	 */	
	public void drawEntity(T entity, int xmin, int ymin, int scale);
	
	/**
	 * Sets the JavaFX {@code Canvas}ses to be used for drawing the entities.
	 * 
	 * @param layers	a {@code Map<Integer, Canvas>} of layers
	 */
	public void setLayers(Map<Integer, Canvas> layers);
	
	/**
	 * Returns the comparator used to decide the z-order of the entities.
	 * 
	 * @return	a {@code Comparator}
	 */
	public Comparator<T> getComparator();
}
