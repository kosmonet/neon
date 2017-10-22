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
import java.util.Map.Entry;
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
	private final int fill;
	
	private PointNode<T> root;
	
	/**
	 * 
	 * @param fill	the maximum amount of elements in a leaf node
	 */
	public PointQuadTree(int x, int y, int width, int height, int fill) {
		this.fill = fill;
	}
	
	/**
	 * 
	 * @param fill	the maximum amount of elements in a leaf node
	 */
	public PointQuadTree(int fill) {
		this.fill = fill;
	}
	
	/**
	 * Inserts a new element at the given position.
	 * 
	 * @param element
	 * @param position
	 */
	public void insert(T element, Point position) {
		if (root == null) {
			// root is null if this is the first element to be inserted
			root = new PointNode<T>(position.x, position.y, 1, fill, elements);
		} else if(!root.contains(position)) {
			// if root isn't big enough to contain the new element, we have to enlarge the tree
			enlargeTree(position);
		}
		
		elements.put(element, position);
		root.insert(element, position);
	}
	
	/**
	 * Enlarges the tree so that it contains the given position.
	 * 
	 * @param position
	 */
	private void enlargeTree(Point position) {
		// find the minimum bounds needed to contain the new position
		Rectangle bounds = root.getBounds().union(new Rectangle(position.x, position.y, 1, 1));
		int size = Math.max(1, Integer.highestOneBit(Math.max(bounds.width, bounds.height) - 1) << 1);
		
		// bounds are somewhat bigger than the actual needed area, calculate how much bigger
		int dx = (size - bounds.width)/2;
		int dy = (size - bounds.height)/2;
		// shift the root node a bit so we have some margin around the needed area for adding other elements
		root = new PointNode<T>(bounds.x - dx, bounds.y - dy, size, fill, elements);

		// add all elements to the new tree again
		for(Entry<T, Point> entry : elements.entrySet()) {
			root.insert(entry.getKey(), entry.getValue());
		}
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
		if(!root.contains(position)) {
			enlargeTree(position);
		}
		
		Point previous = elements.get(element);
		elements.put(element, position);
		root.move(element, position, previous);
	}
}
