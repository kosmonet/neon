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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A node in the quadtree.
 * 
 * @author mdriesen
 * @param <T>
 */
class RegionNode<T> {
	private final int x, y, width, height;
	private T value;
	private RegionNode<T> NW, NE, SW, SE;
	
	/**
	 * Initializes this {@code Node} without an initial value.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	RegionNode(int x, int y, int width, int height) {
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
	RegionNode(int x, int y, int width, int height, T value) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.value = value;
	}
	
	void insert(Rectangle bounds, T value) {
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
				NW = new RegionNode<T>(x, y, width/2, height/2, this.value); 
				NE = new RegionNode<T>(x + width/2, y, width - width/2, height/2, this.value);
				SW = new RegionNode<T>(x, y + height/2, width/2, height - height/2, this.value);
				SE = new RegionNode<T>(x + width/2, y + height/2, width - width/2, height - height/2, this.value);
			}
			
			// and add value to children
			NW.insert(bounds, value);
			NE.insert(bounds, value);
			SW.insert(bounds, value);
			SE.insert(bounds, value);
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
	
	/**
	 * Returns a collection of all leaf nodes contained in this node.
	 * 
	 * @return
	 */
	Collection<RegionNode<T>> getLeaves() {
		// this leaf may have zero area if the quadtree size is not a power of 2
		if(width == 0 || height == 0) {
			return new ArrayList<RegionNode<T>>();
		} else if(isLeaf()) {
			return Arrays.asList(this);
		} else {
			Collection<RegionNode<T>> list = new ArrayList<>();
			list.addAll(NW.getLeaves());
			list.addAll(NE.getLeaves());
			list.addAll(SW.getLeaves());
			list.addAll(SE.getLeaves());
			return list;
		}
	}
	
	private boolean contains(int x, int y) {
		return (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height);
	}
	
	boolean isLeaf() {
		return NW == null;
	}
	
	T getValue() {
		return value;
	}
	
	Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
}