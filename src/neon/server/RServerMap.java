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

package neon.server;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import neon.common.resources.RMap;
import neon.entity.components.ShapeComponent;
import neon.util.quadtree.PointQuadTree;
import neon.util.quadtree.RegionQuadTree;

/**
 * 
 * @author mdriesen
 *
 */
public class RServerMap extends RMap implements Observer {
	/**
	 * The uid of this map.
	 */
	public final int uid;
	/**
	 * The fancy display name.
	 */
	public final String name;
	
	private final RegionQuadTree<String> terrain;
	private final RegionQuadTree<Integer> elevation;
	private final PointQuadTree<Long> entities;
	private final int width, height;
	
	/**
	 * Initializes this map without terrain, elevation or entities.
	 * 
	 * @param id
	 * @param name
	 * @param width
	 * @param height
	 * @param uid
	 */
	public RServerMap(String id, String name, int width, int height, int uid) {
		super(id);
		this.name = name;
		this.width = width;
		this.height = height;
		terrain = new RegionQuadTree<>(width, height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(width,  height, 0);
		entities = new PointQuadTree<>(0, 0, width, height, 100);
		this.uid = uid;
	}
	
	@Override
	public RegionQuadTree<String> getTerrain() {
		return terrain;
	}
	
	@Override
	public RegionQuadTree<Integer> getElevation() {
		return elevation;
	}
	
	/**
	 * Adds an entity to the map in the given position.
	 * 
	 * @param uid
	 * @param x
	 * @param y
	 */
	public void addEntity(long uid, int x, int y) {
		entities.insert(uid, new Point(x, y));
	}
	
	/**
	 * 
	 * @return	all entities on the map
	 */
	public Set<Long> getEntities() {
		return entities.getElements();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return	all entities at the given coordinates
	 */
	public Set<Long> getEntities(int x, int y) {
		return entities.get(new Point(x, y));
	}
	
	/**
	 * 
	 * @param bounds
	 * @return	all entities in the given bounds
	 */
	@Override
	public Set<Long> getEntities(Rectangle bounds) {
		return entities.get(bounds);
	}
	
	private void moveEntity(Long uid, int x, int y) {
		entities.move(uid, new Point(x, y));
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void update(Observable observable, Object arg) {
		if (observable instanceof ShapeComponent) {
			ShapeComponent shape = (ShapeComponent) observable;
			moveEntity(shape.getEntity(), shape.getX(), shape.getY());
		}
	}
}