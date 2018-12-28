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

public interface PointSpatialIndex<E> {
	/** 
	 * 
	 * @param x
	 * @param y
	 * @return	all elements at the given position
	 */
	public Set<E> get(int x, int y);

	/**
	 * 
	 * @param bounds
	 * @return	all elements contained in the given bounds
	 */
	public Set<E> get(Rectangle bounds);
	
	/**
	 * 
	 * @return	all elements in the index
	 */
	public Set<E> getElements();
	
	/**
	 * Inserts an element at the given position.
	 * 
	 * @param element
	 * @param x
	 * @param y
	 */
	public void insert(E element, int x, int y);
	
	/**
	 * Removes an element.
	 * 
	 * @param element
	 */
	public void remove(E element);
	
	/**
	 * Moves an element to a new position.
	 * 
	 * @param element
	 * @param position
	 */
	public void move(E element, Point position);
}
