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

package neon.editor.resource;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import neon.common.resources.Resource;
import neon.util.quadtree.RegionQuadTree;

public class RMap extends Resource {
	/**
	 * The display name.
	 */
	public final String name;
	/**
	 * The uid of this map.
	 */
	public final short uid;
	/**
	 * The module this map belongs to.
	 */
	public final String module;
	
	private final RegionQuadTree<String> terrain;
	private final RegionQuadTree<Integer> elevation;
	private final HashMap<REntity, Integer> entities = new HashMap<>();
	private final Multimap<Point, REntity> positions = HashMultimap.create();
	
	/**
	 * Initializes this map without terrain, elevation or entities.
	 * 
	 * @param id
	 * @param name
	 * @param width
	 * @param height
	 * @param uid
	 * @param module
	 */
	public RMap(String id, String name, int width, int height, short uid, String module) {
		super(id, "map", "maps");
		this.name = name;
		terrain = new RegionQuadTree<>(width, height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(width, height, 0);
		this.uid = uid;
		this.module = module;
	}

	public RegionQuadTree<String> getTerrain() {
		return terrain;
	}
	
	/**
	 * 
	 * @return	the elevation
	 */
	public RegionQuadTree<Integer> getElevation() {
		return elevation;
	}
	
	public int getSize() {
		return terrain.getSize();
	}
	
	/**
	 * Adds an entity to this map.
	 * 
	 * @param entity
	 */
	public void add(REntity entity) {
		entities.put(entity, (int) entity.uid);
		positions.put(new Point(entity.shape.getX(), entity.shape.getY()), entity);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return	all entities in a certain position
	 */
	public Collection<REntity> getEntities(int x, int y) {
		return positions.get(new Point(x, y));
	}
	
	/**
	 * 
	 * @return	all entities on this map
	 */
	public Collection<REntity> getEntities() {
		return entities.keySet();
	}
	
	public void removeEntity(REntity entity) {
		entities.remove(entity);
		positions.remove(new Point(entity.shape.getX(), entity.shape.getY()), entity);
	}
	
	/**
	 * 
	 * @return	an unused uid
	 */
	public int getFreeUID() {
		int i = 0;
		while (entities.containsValue(++i));
		return i;
	}
}
