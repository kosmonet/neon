/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

class RegionNode<T> {
	final int nx, ny, nWidth, nHeight;
	private T value;
	private RegionNode<T> NW, NE, SW, SE;
	
	RegionNode(int x, int y, int width, int height) {
		nx = x;
		ny = y;
		nWidth = width;
		nHeight = height;
	}

	RegionNode(int x, int y, int width, int height, T value) {
		this(x, y, width, height);
		this.value = value;
	}
	
	/**
	 * Inserts a value in a node.
	 * 
	 * @param value
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void insert(T value, int x, int y, int width, int height) {
		// make sure this node overlaps with the given bounds
		if (!overlaps(x, y, width, height)) {
			return;
		} else if (x > nx || y > ny ||  x + width < nx + nWidth || y + height < ny + nHeight) {
			// this node is not fully contained in the given bounds
			if (isLeaf()) {	// check if this is a leaf node
				// check if the given value is the same as the current value (including nulls)
				if (!Objects.equals(value, this.value)) {
					// split the tree
					split();

					// and insert in the child nodes
					store(value, x, y, width, height);

					// sets the value of this node to the most common value of its child nodes
					prune();
				}
			} else {
				// not a leaf, so insert in the child nodes
				store(value, x, y, width, height);

				// check if child nodes can't be merged
				if ((NW == null || (NW.isLeaf() && Objects.equals(NW.value, value))) 
						&& (NE == null || (NE.isLeaf() && Objects.equals(NE.value, value))) 
						&& (SW == null || (SW.isLeaf() && Objects.equals(SW.value, value))) 
						&& (SE == null || (SE.isLeaf() && Objects.equals(SE.value, value)))) {
					merge(value);
				} else {
					// sets the value of this node to the most common value of its child nodes
					prune();
				}
			}
		} else {
			// this node is fully contained, merge child nodes
			merge(value);
		}
	}
	
	/**
	 * Splits a node in four child nodes. Only nodes with width and height larger
	 * than 0 are constructed.
	 */
	private void split() {
		int sx = nx + nWidth/2;
		int sy = ny + nHeight/2;


		if (nx != sx && ny != sy) {
			NW = new RegionNode<>(nx, ny, sx - nx, sy - ny, value);
		}

		if (ny != sy) {
			NE = new RegionNode<>(sx, ny, nWidth - (sx - nx), sy - ny, value);
		}

		if (nx != sx) {
			SW = new RegionNode<>(nx, sy, sx - nx, nHeight - (sy - ny), value);
		}

		SE = new RegionNode<>(sx, sy, nWidth - (sx - nx), nHeight - (sy - ny), value);
	}

	/**
	 * Inserts a value in the child nodes.
	 * 
	 * @param value
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void store(T value, int x, int y, int width, int height) {
		if (NW != null) {
			NW.insert(value, x, y, width, height);
		}
		
		if (NE != null) {
			NE.insert(value, x, y, width, height);
		}
		
		if (SW != null) {
			SW.insert(value, x, y, width, height);
		}
		
		if (SE != null) {
			SE.insert(value, x, y, width, height);		
		}
	}
	
	/**
	 * Merges child nodes back into the parent node.
	 * 
	 * @param value
	 */
	private void merge(T value) {
		this.value = value;
		NW = null;
		NE = null;
		SW = null;
		SE = null;		
	}

	/**
	 * Sets the value of a node to the most common value of its child nodes.
	 */
	private void prune() {
		ArrayList<T> values = new ArrayList<>(4);
		if (NW != null && NW.value != null) { values.add(NW.value); }
		if (NE != null && NE.value != null) { values.add(NE.value);	}
		if (SW != null && SW.value != null) { values.add(SW.value);	}
		if (SE != null && SE.value != null) { values.add(SE.value);	}
		
		values.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
			      .entrySet().stream().max(Comparator.comparing(Entry::getValue))
			      .ifPresent(entry -> value = entry.getKey());
	}
	
	/**
	 * Checks if a node contains a point.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean contains(int x, int y) {
		return x >= nx && y >= ny && x < nx + nWidth && y < ny + nHeight;
	}
	
	/**
	 * Checks if a node overlaps with a rectangle.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	private boolean overlaps(int x, int y, int width, int height) {
		return !(x > nx + nWidth - 1 || y > ny + nHeight - 1 || x + width - 1 < nx || y + height - 1 < ny);
	}
	
	T get(int x, int y) {
		if (!isLeaf()) {
			if (NW != null && NW.contains(x, y)) { 
				return NW.get(x, y); 
			} else if (NE != null && NE.contains(x, y)) { 
				return NE.get(x, y); 
			} else if (SW != null && SW.contains(x, y)) { 
				return SW.get(x, y); 
			} else if (SE != null && SE.contains(x, y)) { 
				return SE.get(x, y); 
			} else {
				throw new AssertionError("Coordinates should not be outside child bounds!");
			}
		} else {
			return value;
		}
	}

	/**
	 * Returns the value of a node.
	 * 
	 * @return
	 */
	T getValue() {
		return value;
	}
	
	boolean isLeaf() {
		return NW == null && NE == null && SW == null && SE == null;
	}
	
	Collection<RegionNode<T>> getLeaves() {
		if (nWidth == 0 || nHeight == 0) {
			throw new AssertionError("Empty leaves should not exist!");
		} else if (isLeaf()) {
			return Arrays.asList(this);
		} else {
			Collection<RegionNode<T>> list = new ArrayList<>();
			list.addAll(NW != null ? NW.getLeaves() : Collections.emptyList());
			list.addAll(NE != null ? NE.getLeaves() : Collections.emptyList());
			list.addAll(SW != null ? SW.getLeaves() : Collections.emptyList());
			list.addAll(SE != null ? SE.getLeaves() : Collections.emptyList());
			return list;
		}		
	}
}
