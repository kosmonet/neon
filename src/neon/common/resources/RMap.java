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

package neon.common.resources;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import neon.util.spatial.PointQuadTree;
import neon.util.spatial.PointSpatialIndex;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

/**
 * @author mdriesen
 */
public final class RMap extends Resource {
	
	/**
	 * The uid of this map.
	 */
	public final int uid;
	/**
	 * The fancy display name.
	 */
	public final String name;
	
	public final int width, height;
	
	private final RegionSpatialIndex<String> terrain;
	private final RegionSpatialIndex<Integer> elevation;
	private final PointSpatialIndex<Long> entities;
	
	/**
	 * Initializes this map without terrain, elevation or entities.
	 * 
	 * @param id
	 * @param name
	 * @param width
	 * @param height
	 * @param uid
	 */
	public RMap(String id, String name, int width, int height, int uid) {
		super(id, "maps");
		this.name = name;
		this.width = width;
		this.height = height;
		terrain = new RegionQuadTree<>(width, height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(width,  height, 0);
		entities = new PointQuadTree<>(0, 0, width, height, 100);
		this.uid = uid;
	}
	
	public RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}
	
	public RegionSpatialIndex<Integer> getElevation() {
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
		entities.insert(uid, x, y);
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
		return entities.get(x, y);
	}
	
	/**
	 * 
	 * @param bounds
	 * @return	all entities in the given bounds
	 */
	public Set<Long> getEntities(Rectangle bounds) {
		return entities.get(bounds);
	}
	
	public void moveEntity(long uid, int x, int y) {
		entities.move(uid, new Point(x, y));
	}
	
	/**
	 * Removes an entity from the map.
	 * 
	 * @param uid
	 */
	public void removeEntity(long uid) {
		entities.remove(uid);
	}
}
