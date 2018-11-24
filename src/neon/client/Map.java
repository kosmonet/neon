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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.jdom2.Element;

import com.google.common.collect.ImmutableSet;

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
	private final Collection<Marker> markers = new ArrayList<>();
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
		initMarkers(root.getChild("labels"));
	}
	
	/**
	 * Returns the id of the resource a map was derived from.
	 * 
	 * @return
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * 
	 * @return	the width of the map
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * 
	 * @return	the height of the map
	 */
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

	/**
	 * Returns all entities at the given position.
	 * 
	 * @param x
	 * @param y
	 * @return	a {@code Collection} of entity uid's
	 */
	public Collection<Long> getEntities(int x, int y) {
		return entities.get(x, y);
	}

	/**
	 * Returns all entities on the map.
	 * 
	 * @return	a {@code Collection} of entity uid's
	 */
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
			this.terrain.insert(id, x, y, width, height);
		}
	}
	
	private void initElevation(Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int z = Integer.parseInt(region.getAttributeValue("z"));
			this.elevation.insert(z, x, y, width, height);
		}
	}
	
	public RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}
	
	public RegionSpatialIndex<Integer> getElevation() {
		return elevation;
	}
	
	private void initMarkers(Element labels) {
		for (Element label : labels.getChildren()) {
			int x = Integer.parseInt(label.getAttributeValue("x"));
			int y = Integer.parseInt(label.getAttributeValue("y"));
			String text = label.getText();
			markers.add(new Marker(x, y, text));
		}
	}
	
	/**
	 * 
	 * @return	an unmodifiable {@code Collection} of {@code Marker}s
	 */
	public Collection<Marker> getMarkers() {
		return ImmutableSet.copyOf(markers);
	}
	
	public static final class Marker {
		public final int x;
		public final int y;
		public final String text;
		
		private Marker(int x, int y, String text) {
			this.x = x;
			this.y = y;
			this.text = text;
		}
	}
}