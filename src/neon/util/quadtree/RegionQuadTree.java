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
import java.util.HashMap;
import java.util.Map;

/**
 * A region quadtree.
 * 
 * @author mdriesen
 * @param <T>
 */
public class RegionQuadTree<T> {
	private final int size;
	private final RegionNode<T> root;
	
	/**
	 * Initializes the tree with the given size.
	 * 
	 * @param width
	 * @param height
	 */
	public RegionQuadTree(int width, int height) {
		this(width, height, null);
	}
	
	/**
	 * Initializes the tree with the given size and initial value.
	 * 
	 * @param width
	 * @param height
	 * @param value
	 */
	public RegionQuadTree(int width, int height, T value) {
		// make a square tree with power of two size
		int size = Math.max(1, Integer.highestOneBit(Math.max(width, height) - 1) << 1);
		if(size < 1) {
			throw new IllegalArgumentException("quadtree width and height must be larger than 0");
		} else {
			this.size = size;
			root = new RegionNode<T>(0, 0, size, size, value);
		}
	}
	
	/**
	 * Adds a value to the tree.
	 * 
	 * @param bounds
	 * @param value
	 */
	public void insert(Rectangle bounds, T value) {
		root.insert(bounds, value);
	}
	
	/**
	 * Returns the value at coordinates (x, y). Returns null if the coordinates
	 * fall outside the quadtree area, or if no value was added at the given
	 * coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public T get(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size) {
			return null;
		} else {
			return root.get(x, y);
		}
	}
	
	public int getSize() {
		return size;
	}
	
	public Map<Rectangle, T> getLeaves() {
		Map<Rectangle, T> leaves = new HashMap<>();
		if(!root.isLeaf()) {
			for(RegionNode<T> node : root.getLeaves()) {
				leaves.put(node.getBounds(), node.getValue());
			}
		}
		return leaves;
	}
}
