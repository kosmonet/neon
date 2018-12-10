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

package neon.server.entity;

import java.awt.Point;
import java.io.IOException;
import java.util.Collection;

import neon.common.resources.RMap;
import neon.util.spatial.PointQuadTree;
import neon.util.spatial.PointSpatialIndex;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

public final class Map {
	private final RMap map;
	private final RegionSpatialIndex<String> terrain;
	private final RegionSpatialIndex<Integer> elevation;
	private final PointSpatialIndex<Long> entities;
	private final int uid;

	public Map(RMap map, int uid) throws IOException {
		this.map = map;
		this.uid = uid;
		
		terrain = new RegionQuadTree<>(map.width, map.height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(map.width,  map.height, 0);
		entities = new PointQuadTree<>(0, 0, map.width, map.height, 100);
	}
	
	public int getUID() {
		return uid;
	}
	
	public String getID() {
		return map.id;
	}
	
	public int getWidth() {
		return map.width;
	}
	
	public int getHeight() {
		return map.height;
	}
	
	public String getTerrain(int x, int y) {
		return terrain.get(x, y);
	}
	
	RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}
	
	RegionSpatialIndex<Integer> getElevation() {
		return elevation;
	}
	
	public Iterable<Long> getEntities(int x, int y) {
		return entities.get(x, y);
	}
	
	public Collection<Long> getEntities() {
		return entities.getElements();
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
	 * Moves an entity to the given position.
	 * 
	 * @param uid
	 * @param x
	 * @param y
	 */
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
