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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A node in the quadtree.
 * 
 * @author mdriesen
 * @param <T>
 */
class PointNode<T> {
	private final Map<T, Point> elements;
	private final int fill;
	private final int x, y, width, height;
	
	private PointNode<T> NW, NE, SE, SW;
	
	PointNode(int x, int y, int width, int height, int fill) {
		this.fill = fill;
		this.x = x;
		this.y = y;
		this. width = width;
		this.height = height;
		elements = new HashMap<>(fill);
	}
	
	void insert(T element, Point position) {
		// check if this node is full
		if(elements.size() < fill) {
			// if not, add element to this node
			elements.put(element, position);			
		} else {
			// if full, split node
			NW = new PointNode<T>(x, y, width/2, height/2, fill); 
			NE = new PointNode<T>(x + width/2, y, width - width/2, height/2, fill);
			SW = new PointNode<T>(x, y + height/2, width/2, height - height/2, fill);
			SE = new PointNode<T>(x + width/2, y + height/2, width - width/2, height - height/2, fill);
			
			// and add elements to the child nodes
			for (Entry<T, Point> entry : elements.entrySet()) {
				if (NW.contains(entry.getValue())) {
					NW.insert(entry.getKey(), entry.getValue());
				} else if (NE.contains(entry.getValue())) {
					NE.insert(entry.getKey(), entry.getValue());
				} else if (SW.contains(entry.getValue())) {
					SW.insert(entry.getKey(), entry.getValue());
				} else if (SE.contains(entry.getValue())) {
					SE.insert(entry.getKey(), entry.getValue());
				}
			}
			
			// don't forget to clear this node, it's no longer a leaf
			elements.clear();
		}
	}
	
	Set<T> get(Rectangle bounds) {
		Set<T> set = new HashSet<T>();

		if(bounds.intersects(x, y, width, height)) {
			if (isLeaf())  {
				for (Entry<T, Point> entry : elements.entrySet()) {
					if (bounds.contains(entry.getValue())) {
						set.add(entry.getKey());
					}
				}			
			} else {
				set.addAll(NW.get(bounds));
				set.addAll(NE.get(bounds));
				set.addAll(SE.get(bounds));
				set.addAll(SW.get(bounds));
			}
		}

		return set;
	}
	
	Set<T> get(Point position) {
		if(!contains(position)) {
			return new HashSet<T>();
		} else if (isLeaf()) {
			Set<T> set = new HashSet<T>();
			for (Entry<T, Point> entry : elements.entrySet()) {
				if (entry.getValue().equals(position)) {
					set.add(entry.getKey());
				}
			}
			return set;
		} else {
			if (NW.contains(position)) {
				return NW.get(position);
			} else if (NE.contains(position)) {
				return NE.get(position);
			} else if (SE.contains(position)) {
				return SE.get(position);
			} else {
				return SW.get(position);
			}
		}
	}
	
	private boolean isLeaf() {
		return NW == null;
	}
	
	private boolean contains(Point position) {
		return new Rectangle(x, y, width, height).contains(position);
	}
	
	Set<T> getAll() {
		if(isLeaf()) {
			return elements.keySet();
		} else {
			Set<T> set = new HashSet<T>();
			set.addAll(NW.getAll());
			set.addAll(NE.getAll());
			set.addAll(SE.getAll());
			set.addAll(SW.getAll());
			return set;
		}
	}
}
