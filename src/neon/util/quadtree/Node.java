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

import java.awt.Rectangle;

/**
 * A node in the quadtree.
 * 
 * @author mdriesen
 *
 * @param <T>
 */
class Node<T> {
	private final int x, y, width, height;
	private T value;
	private Node<T> NW, NE, SW, SE;
	
	/**
	 * Initializes this {@code Node} without an initial value.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	Node(int x, int y, int width, int height) {
		this(x, y, width, height, null);
	}
	
	/**
	 * Initializes this {@code Node} with an initial value.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param value
	 */
	Node(int x, int y, int width, int height, T value) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.value = value;
	}
	
	void add(Rectangle bounds, T value) {
		// if this node is completely contained in the bounds, set this as a leaf and discard children
		if (bounds.contains(x, y, width, height)) {
			this.value = value;
			NW = null;
			NE = null;
			SW = null;
			SE = null;
		} else if (bounds.intersects(x, y, width, height)) {
			// if this was a leaf, create children
			if (isLeaf()) {
				NW = new Node<T>(x, y, width/2, height/2, this.value); 
				NE = new Node<T>(x + width/2, y, width - width/2, height/2, this.value);
				SW = new Node<T>(x, y + height/2, width/2, height - height/2, this.value);
				SE = new Node<T>(x + width/2, y + height/2, width - width/2, height - height/2, this.value);
			}
			
			// and add value to children
			NW.add(bounds, value);
			NE.add(bounds, value);
			SW.add(bounds, value);
			SE.add(bounds, value);
		}
	}
	
	/**
	 * Returns the value at coordinate (x, y).
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	T get(int x, int y) {
		if (!isLeaf()) {
			if (NW.contains(x, y)) { return NW.get(x, y); }
			if (NE.contains(x, y)) { return NE.get(x, y); }
			if (SW.contains(x, y)) { return SW.get(x, y); }
			if (SE.contains(x, y)) { return SE.get(x, y); }
		} 

		return value;
	}
	
	private boolean contains(int x, int y) {
		return (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height);
	}
	
	private boolean isLeaf() {
		return NW == null;
	}
}
