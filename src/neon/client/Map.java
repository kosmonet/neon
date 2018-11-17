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

package neon.client;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collection;

import org.jdom2.Element;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RMap;
import neon.util.spatial.PointQuadTree;
import neon.util.spatial.PointSpatialIndex;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

public class Map {
	private static final XMLTranslator translator = new XMLTranslator();

	private final RegionSpatialIndex<String> terrain;
	private final RegionSpatialIndex<Integer> elevation;
	private final PointSpatialIndex<Long> entities;
	private final String id;
	private final int width, height;
	
	public Map(RMap map, NeonFileSystem files) throws IOException {
		this.id = map.id;
		this.width = map.width;
		this.height = map.height;
		
		entities = new PointQuadTree<>(0, 0, map.width, map.height, 100);
		terrain = new RegionQuadTree<>(map.width, map.height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(map.width,  map.height, 0);

		Element root = files.loadFile(translator, "maps", map.id + ".xml").getRootElement();
		initTerrain(root.getChild("terrain"));
		initElevation(root.getChild("elevation"));
	}
	
	public String getID() {
		return id;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
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

	public Collection<Long> getEntities(int x, int y) {
		return entities.get(x, y);
	}

	public Collection<Long> getEntities() {
		return entities.getElements();
	}
	
	/**
	 * Removes an entity from the map.
	 * 
	 * @param uid
	 */
	public void removeEntity(long uid) {
		entities.remove(uid);
	}
	
	private void initTerrain(Element terrain) {
		for (Element region : terrain.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			String id = region.getAttributeValue("id");
			this.terrain.insert(new Rectangle(x, y, width, height), id);
		}
	}
	
	private void initElevation(Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int value = Integer.parseInt(region.getAttributeValue("v"));
			this.elevation.insert(new Rectangle(x, y, width, height), value);
		}		
	}
	
	public RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}
	
	public RegionSpatialIndex<Integer> getElevation() {
		return elevation;
	}
}
