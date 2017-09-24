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
 *
 * @param <T>
 */
public class RegionQuadTree<T> {
	private final int width, height;
	private final Node<T> root;
	
	/**
	 * Initializes the tree with the given size.
	 * 
	 * @param width
	 * @param height
	 */
	public RegionQuadTree(int width, int height) {
		if(width < 1 || height < 1) {
			throw new IllegalArgumentException("quadtree width and height must be larger than 0");
		}		

		this.width = width;
		this.height = height;
		int size = Math.max(width, height);
		// make a square tree
		root = new Node<T>(0, 0, size, size);
	}
	
	/**
	 * Initializes the tree with the given size and initial value.
	 * 
	 * @param width
	 * @param height
	 * @param value
	 */
	public RegionQuadTree(int width, int height, T value) {
		if(width < 1 || height < 1) {
			throw new IllegalArgumentException("quadtree width and height must be larger than 0");
		}		

		this.width = width;
		this.height = height;
		int size = Math.max(width, height);
		// make a square tree
		root = new Node<T>(0, 0, size, size, value);
	}
	
	/**
	 * Adds a value to the tree.
	 * 
	 * @param bounds
	 * @param value
	 */
	public void add(Rectangle bounds, T value) {
		root.add(bounds, value);
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
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return null;
		} else {
			return root.get(x, y);
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Map<Rectangle, T> getLeaves() {
		Map<Rectangle, T> leaves = new HashMap<>();
		if(!root.isLeaf()) {
			for(Node<T> node : root.getLeaves()) {
				leaves.put(node.getBounds(), node.getValue());
			}
		}
		return leaves;
	}
}
