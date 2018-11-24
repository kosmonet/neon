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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

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
	
	void insert(T value, int x, int y, int width, int height) {
		// make sure this node overlaps with the given bounds
		if (x > nx + nWidth - 1 || y > ny + nHeight - 1 || x + width - 1 < nx || y + height - 1 < ny) {
			return;
		} else if (x > nx || y > ny ||  x + width < nx + nWidth || y + height < ny + nHeight) {
			// this node is not fully contained in the given bounds
			if (isLeaf()) {	// check if this is a leaf node
				// check if the given value is the same as the current value (including nulls)
				if (!Objects.equals(value, this.value)) {
					// split the tree
					split();

					// and insert in the subnodes
					NW.insert(value, x, y, width, height);
					NE.insert(value, x, y, width, height);
					SW.insert(value, x, y, width, height);
					SE.insert(value, x, y, width, height);
				}
			} else {
				// not a leaf, so insert in the subnodes
				NW.insert(value, x, y, width, height);
				NE.insert(value, x, y, width, height);
				SW.insert(value, x, y, width, height);
				SE.insert(value, x, y, width, height);

				// check if subnodes can't be merged
				if (NW.isLeaf() && NE.isLeaf() && SW.isLeaf() && SE.isLeaf()) {
					if (Objects.equals(NW.value, NE.value) && Objects.equals(NE.value, SW.value) && Objects.equals(SW.value, SE.value)) {
						merge(value);
					}
				}
			}			
		} else {
			// if this node is fully contained, merge subnodes
			merge(value);
		}
	}
	
	private void merge(T value) {
		this.value = value;
		NW = null;
		NE = null;
		SW = null;
		SE = null;		
	}

	private boolean contains(int x, int y) {
		return x >= nx && y >= ny && x < nx + nWidth && y < ny + nHeight;
	}
	
	T get(int x, int y) {
		if (!isLeaf()) {
			if (NW.contains(x, y)) { 
				return NW.get(x, y); 
			} else if (NE.contains(x, y)) { 
				return NE.get(x, y); 
			} else if (SW.contains(x, y)) { 
				return SW.get(x, y); 
			} else if (SE.contains(x, y)) { 
				return SE.get(x, y); 
			}
		} 

		return value;
	}

	T getValue() {
		return value;
	}
	
	private void split() {
		int sx = nx + nWidth/2;
		int sy = ny + nHeight/2;


//		if (x != sx && y != sy) {
			NW = new RegionNode<>(nx, ny, sx - nx, sy - ny, value);
//		}

//		if (y != sy) {
			NE = new RegionNode<>(sx, ny, nWidth - (sx - nx), sy - ny, value);
//		}

//		if (x != sx) {
			SW = new RegionNode<>(nx, sy, sx - nx, nHeight - (sy - ny), value);
//		}

		SE = new RegionNode<>(sx, sy, nWidth - (sx - nx), nHeight - (sy - ny), value);
	}

	boolean isLeaf() {
		return NW == null && NE == null && SW == null && SE == null;
	}
	
	Collection<RegionNode<T>> getLeaves() {
		// this leaf may have zero area if the quadtree size is not a power of 2
		if(nWidth == 0 || nHeight == 0) {
			return Collections.emptyList();
		} else if (isLeaf()) {
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
}
