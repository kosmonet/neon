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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

/**
 * A spatial index that stores single elements at a certain x and y coordinate.
 * 
 * @author mdriesen
 * @param <E>	the type of element to store
 */
public interface PointSpatialIndex<E> {
	/** 
	 * Returns all elements at the given position
	 * 
	 * @param x	the x coordinate of the position
	 * @param y	the y coordinate of the position
	 * @return	a {@code Set} of elements
	 */
	public Set<E> get(int x, int y);

	/**
	 * Returns all elements within certain rectangular bounds.
	 * 
	 * @param bounds	the {@code Rectangle} to search in
	 * @return	a {@code Set} of elements
	 */
	public Set<E> get(Rectangle bounds);
	
	/**
	 * Returns all elements in this index.
	 * 
	 * @return	a {@code Set} of elements
	 */
	public Set<E> getElements();
	
	/**
	 * Inserts an element at the given position.
	 * 
	 * @param element	the element to insert
	 * @param x	the x coordinate to insert in
	 * @param y	the y coordinate to insert in
	 */
	public void insert(E element, int x, int y);
	
	/**
	 * Removes an element.
	 * 
	 * @param element	the element to remove
	 */
	public void remove(E element);
	
	/**
	 * Moves an element to a new position.
	 * 
	 * @param element	the element to move
	 * @param position	the new position
	 */
	public void move(E element, Point position);
}
