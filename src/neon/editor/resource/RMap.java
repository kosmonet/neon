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

package neon.editor.resource;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import neon.common.graphics.RenderableMap;
import neon.common.resources.Resource;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

/**
 * A map resource that can be directly rendered.
 * 
 * @author mdriesen
 *
 */
public final class RMap extends Resource implements RenderableMap<REntity> {
	/** The display name. */
	public final String name;
	/** The uid of this map. */
	public final short uid;
	/** The module this map belongs to. */
	public final String module;
	
	private final RegionSpatialIndex<String> terrain;
	private final RegionSpatialIndex<Integer> elevation;
	private final HashMap<REntity, Integer> entities = new HashMap<>();
	private final Multimap<Point, REntity> positions = HashMultimap.create();
	
	/**
	 * Initializes this map without terrain, elevation or entities.
	 * 
	 * @param id	the id of the map
	 * @param name	the displayed name of the map
	 * @param width	the width of the map
	 * @param height	the height of the map
	 * @param uid	the uid of the map
	 * @param module	the id of the module this map belongs to
	 */
	public RMap(String id, String name, int width, int height, short uid, String module) {
		super(id, "maps");
		this.name = name;
		terrain = new RegionQuadTree<>(width, height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(width, height, 0);
		this.uid = uid;
		this.module = module;
	}

	@Override
	public RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}
	
	/**
	 * 
	 * @return	the elevation
	 */
	@Override
	public RegionSpatialIndex<Integer> getElevation() {
		return elevation;
	}
	
	/**
	 * Returns the width of the map.
	 * 
	 * @return	the width
	 */
	public int getWidth() {
		return terrain.getWidth();
	}
	
	/**
	 * Returns the height of the map.
	 * 
	 * @return	the height
	 */
	public int getHeight() {
		return terrain.getHeight();
	}
	
	/**
	 * Adds an entity to this map.
	 * 
	 * @param entity	the {@code REntity} to add
	 */
	public void add(REntity entity) {
		entities.put(entity, (int) entity.uid);
		positions.put(new Point(entity.shape.getX(), entity.shape.getY()), entity);
	}
	
	/**
	 * Returns all entities in a certain position.
	 * 
	 * @param x	the x coordinate of the position
	 * @param y	the y coordinate of the position
	 * @return	a {@code Collection<REntity>}
	 */
	public Collection<REntity> getEntities(int x, int y) {
		return positions.get(new Point(x, y));
	}
	
	@Override
	public Collection<REntity> getEntities() {
		return entities.keySet();
	}
	
	/**
	 * Removes an entity from this map.
	 * 
	 * @param entity	the {@code REntity} to remove
	 */
	public void removeEntity(REntity entity) {
		entities.remove(entity);
		positions.remove(new Point(entity.shape.getX(), entity.shape.getY()), entity);
	}
	
	/**
	 * Returns an unused uid.
	 * 
	 * @return	an unused uid
	 */
	public int getFreeUID() {
		int i = 0;
		while (entities.containsValue(++i));
		return i;
	}
}
