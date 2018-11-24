/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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
import java.util.HashMap;
import java.util.Map;

public class RegionQuadTree<T> implements RegionSpatialIndex<T> {
	private final RegionNode<T> root;
	
	public RegionQuadTree(int width, int height) {
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException("Width and height should be larger than 0.");
		} else {
			root = new RegionNode<>(0, 0, width, height);
		}
	}
	
	public RegionQuadTree(int width, int height, T initialValue) {
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException("Width and height should be larger than 0.");
		} else {
			root = new RegionNode<>(0, 0, width, height, initialValue);
		}
	}
	
	public void insert(T value, int x, int y, int width, int height) {
		if (contains(width, height, x, y)) {
			root.insert(value, x, y, width, height);
		} else {
			System.out.println("value: " + x + ", " + y + ", " + width + ", " + height);
			throw new IndexOutOfBoundsException("Insertion does not fit in bounds.");
		}
	}
	
	public T get(int x, int y) {
		if (contains(x, y, 1, 1)) {
			return root.get(x, y);
		} else {
			throw new IndexOutOfBoundsException("Query does not fit in bounds.");
		}
	}

	public int getWidth() {
		return root.nWidth;
	}
	
	public int getHeight() {
		return root.nHeight;
	}
	
	private boolean contains(int x, int y, int width, int height) {
		return !(x < 0 || y < 0 || (x + width) > root.nWidth || (y + height) > root.nHeight);
	}

	@Override
	public Map<Rectangle, T> getElements() {
		Map<Rectangle, T> leaves = new HashMap<>();
		for (RegionNode<T> node : root.getLeaves()) {
			if (node.getValue() != null) {
				leaves.put(new Rectangle(node.nx, node.ny, node.nWidth, node.nHeight), node.getValue());
			}
		}
		return leaves;
	}

	@Override
	public void insert(Rectangle bounds, T value) {
		// TODO Auto-generated method stub
		insert(value, bounds.x, bounds.y, bounds.width, bounds.height);
	}
}
