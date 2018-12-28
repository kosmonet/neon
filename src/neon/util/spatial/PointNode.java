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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A node in the quadtree.
 * 
 * @author mdriesen
 * @param <E>
 */
class PointNode<E> {
	private final Map<E, Point> elements;
	private final int fill;
	private final int x, y, size;
	private final Set<E> contents;
	
	private PointNode<E> NW, NE, SE, SW;
	
	PointNode(int x, int y, int size, int fill, Map<E, Point> elements) {
		this.fill = fill;
		this.x = x;
		this.y = y;
		this.size = size;
		this.elements = elements;
		contents = new HashSet<>(fill);
	}
	
	/**
	 * Inserts an element in this node. This method does not check whether the 
	 * node actually contains the element, it is assumed the parent node takes
	 * care of that.
	 * 
	 * @param element
	 * @param position
	 */
	void insert(E element, Point position) {
		// check if this node is full
		if (contents.size() < fill) {
			// if not, add element to this node
			contents.add(element);
		} else if (size < 2) {
			// if the node can't be split any more, add to this node anyway
			contents.add(element);			
		} else {
			// if full, split node
			NW = new PointNode<E>(x, y, size/2, fill, elements); 
			NE = new PointNode<E>(x + size/2, y, size/2, fill, elements);
			SW = new PointNode<E>(x, y + size/2, size/2, fill, elements);
			SE = new PointNode<E>(x + size/2, y + size/2, size/2, fill, elements);
			
			// and add elements to the child nodes
			for (E el : contents) {
				if (NW.contains(elements.get(el))) {
					NW.insert(el, elements.get(el));
				} else if (NE.contains(elements.get(el))) {
					NE.insert(el, elements.get(el));
				} else if (SW.contains(elements.get(el))) {
					SW.insert(el, elements.get(el));
				} else if (SE.contains(elements.get(el))) {
					SE.insert(el, elements.get(el));
				}
			}
			
			// don't forget to clear this node, it's no longer a leaf
			contents.clear();
		}
	}
	
	/**
	 * 
	 * @param bounds
	 * @return	all elements within the given bounds
	 */
	Set<E> get(Rectangle bounds) {
		Set<E> set = new HashSet<E>();

		if(bounds.intersects(x, y, size, size)) {
			if (isLeaf())  {
				for (E element : contents) {
					if (bounds.contains(elements.get(element))) {
						set.add(element);
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
	
	/**
	 * 
	 * @param position
	 * @return	all elements at the given position
	 */
	Set<E> get(Point position) {
		if(!contains(position)) {
			return new HashSet<E>();
		} else if (isLeaf()) {
			Set<E> set = new HashSet<E>();
			for (E element : contents) {
				if (elements.get(element).equals(position)) {
					set.add(element);
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
	
	Rectangle getBounds() {
		return new Rectangle(x, y, size, size);
	}
	
	private boolean isLeaf() {
		return NW == null;
	}
	
	boolean contains(Point position) {
		return new Rectangle(x, y, size, size).contains(position);
	}

	/**
	 * Tries to remove an element from this node.
	 * 
	 * @param element
	 * @param position
	 */
	void remove(E element, Point position) {
		// check if this node contained the old position
		if (contains(position)) {
			// check if this is a leaf
			if (isLeaf()) {
				contents.remove(element);
			} else {
				// if this was not a leaf, check the child nodes
				if (NW.contains(position)) {
					NW.remove(element, position);
				} else if (NE.contains(position)) {
					NE.remove(element, position);
				} else if (SE.contains(position)) {
					SE.remove(element, position);
				} else if (SW.contains(position)) {
					SW.remove(element, position);
				} else {
					throw new IllegalArgumentException("Could not remove element in tree.");
				}
			}
		}
	}
	
	/**
	 * Tries to move an element to a new position.
	 * 
	 * @param element
	 * @param newPos
	 * @param oldPos
	 * @return	{@code true} if the element is still contained in this node after the 
	 * 			move, {@code false} if the parent should try to re-insert the element 
	 * 			at a higher level in the tree
	 */
	boolean move(E element, Point newPos, Point oldPos) {
		// check if this node contained the old position
		if (contains(oldPos)) {
			// check if this is a leaf
			if (isLeaf()) {
				if (contains(newPos)) {
					// if this node also contains the new position of the element, no further action is needed
					return true;
				} else {
					// if this node does not contain the new position, let the parent node handle re-insertion
					contents.remove(element);
					return false;
				}
			} else {
				// if this was not a leaf, check the child nodes
				if (NW.contains(oldPos)) {
					return stuff(NW, element, newPos, oldPos);
				} else if (NE.contains(oldPos)) {
					return stuff(NE, element, newPos, oldPos);
				} else if (SE.contains(oldPos)) {
					return stuff(SE, element, newPos, oldPos);
				} else if (SW.contains(oldPos)) {
					return stuff(SW, element, newPos, oldPos);
				} else {
					throw new IllegalArgumentException("Could not move element in tree.");
				}
			}
		} else {
			return false;
		}
	}

	private boolean stuff(PointNode<E> node, E element, Point oldPos, Point newPos) {
		if (node.move(element, newPos, oldPos)) {
			return true;
		} else {
			// if the child node doesn't contain the new position any longer, try to re-insert
			if (contains(newPos)) {
				insert(element, newPos);
				return true;
			} else {
				return false;
			}
		}
	}
}
