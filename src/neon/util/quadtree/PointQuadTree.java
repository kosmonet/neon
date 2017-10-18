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

package neon.util.quadtree;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * TODO: naam veranderen, is eigenlijk PR quadtree
 * 
 * @author mdriesen
 * @param <T>
 */
public class PointQuadTree<T> {
	private final Map<T, Point> elements = new HashMap<>();
	private final PointNode<T> root;
	
	/**
	 * 
	 * @param fill
	 */
	public PointQuadTree(int size, int fill) {
		root = new PointNode<>(0, 0, size, size, fill, elements);
	}
	
	/**
	 * Inserts a new element at the given position.
	 * 
	 * @param element
	 * @param position
	 */
	public void insert(T element, Point position) {
		elements.put(element, position);
		root.insert(element, position);
	}
	
	/**
	 * 
	 * @param position
	 * @return	all elements at the given position
	 */
	public Set<T> get(Point position) {
		return root.get(position);
	}
	
	/**
	 * 
	 * @param bounds
	 * @return	all elements within the given bounds
	 */
	public Set<T> get(Rectangle bounds) {
		return root.get(bounds);
	}
	
	/**
	 * 
	 * @return	all elements in the tree
	 */
	public Set<T> getElements() {
		return elements.keySet();
	}
	
	/**
	 * Tries to move an element to a new position.
	 * 
	 * @param element
	 * @param position
	 */
	public void move(T element, Point position) {
		Point previous = elements.get(element);
		elements.put(element, position);
		root.move(element, position, previous);
	}
}
