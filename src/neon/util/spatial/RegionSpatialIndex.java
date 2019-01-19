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

package neon.util.spatial;

import java.awt.Rectangle;
import java.util.Map;

/**
 * A spatial index that contains regions filled with the same type of element, 
 * such as a region quadtree.
 * 
 * @author mdriesen
 * @param <E>	the type of elements in the index
 */
public interface RegionSpatialIndex<E> {
	/**
	 * Returns all contiguous regions in the index.
	 * 
	 * @return	a {@code Map<Rectangle, E>} of elements
	 */
	public Map<Rectangle, E> getElements();
	
	/**
	 * Inserts an element with the given bounds.
	 * 
	 * @param value	the value of the element
	 * @param x	the x coordinate of the top left corner of the bounds
	 * @param y	the y coordinate of the top left corner of the bounds
	 * @param width	the width of the bounds
	 * @param height	the height of the bounds
	 */
	public void insert(E value, int x, int y, int width, int height);
	
	/**
	 * Returns the value at the given position.
	 * 
	 * @param x the x coordinate of the position
	 * @param y	the y coordinate of the position
	 * @return	the value
	 */
	public E get(int x, int y);
	
	/**
	 * Returns the width of this spatial index
	 * 
	 * @return	the width
	 */
	public int getWidth();
	
	/**
	 * Returns the height of this spatial index.
	 * 
	 * @return	the height
	 */
	public int getHeight();
}
