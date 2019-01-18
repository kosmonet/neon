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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import com.google.common.collect.ImmutableSet;

import neon.common.graphics.RenderableMap;
import neon.common.resources.RMap;
import neon.util.spatial.PointQuadTree;
import neon.util.spatial.PointSpatialIndex;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

/**
 * Client implementation of a map.
 * 
 * @author mdriesen
 */
public final class Map implements RenderableMap<Long> {
	private static final Logger logger = Logger.getGlobal();

	private final RegionSpatialIndex<String> terrain;
	private final RegionSpatialIndex<Integer> elevation;
	private final PointSpatialIndex<Long> entities;
	private final Collection<Marker> markers = new ArrayList<>();
	private final String id;
	
	/**
	 * Initializes a new map.
	 * 
	 * @param map
	 * @param root
	 */
	public Map(RMap map, Element root) {
		this.id = map.id;
		
		entities = new PointQuadTree<>(0, 0, map.width, map.height, 100);
		terrain = new RegionQuadTree<>(map.width, map.height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(map.width,  map.height, 0);

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
		return terrain.getWidth();
	}
	
	/**
	 * 
	 * @return	the height of the map
	 */
	public int getHeight() {
		return terrain.getHeight();
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
	@Override
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
	
	/**
	 * Initializes the terrain on this map.
	 * 
	 * @param terrain
	 */
	private void initTerrain(Element terrain) {
		for (Element region : terrain.getChildren("region")) {
			try {
				int width = region.getAttribute("w").getIntValue();
				int height = region.getAttribute("h").getIntValue();
				int x = region.getAttribute("x").getIntValue();
				int y = region.getAttribute("y").getIntValue();
				String id = region.getAttributeValue("id");
				this.terrain.insert(id, x, y, width, height);
			} catch (DataConversionException e) {
				logger.severe("failed to load terrain: " + e.getMessage());
			}
		}
	}

	/**
	 * Initializes the height map.
	 * 
	 * @param elevation
	 */
	private void initElevation(Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			try {
				int width = region.getAttribute("w").getIntValue();
				int height = region.getAttribute("h").getIntValue();
				int x = region.getAttribute("x").getIntValue();
				int y = region.getAttribute("y").getIntValue();
				int z = region.getAttribute("z").getIntValue();
				this.elevation.insert(z, x, y, width, height);
			} catch (DataConversionException e) {
				logger.severe("failed to load elevation: " + e.getMessage());
			}
		}
	}

	@Override
	public RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}

	@Override
	public RegionSpatialIndex<Integer> getElevation() {
		return elevation;
	}

	/**
	 * Initializes map markers.
	 * 
	 * @param labels
	 */
	private void initMarkers(Element labels) {
		for (Element label : labels.getChildren()) {
			try {
				int x = label.getAttribute("x").getIntValue();
				int y = label.getAttribute("y").getIntValue();
				String text = label.getText();
				markers.add(new Marker(x, y, text));
			} catch (DataConversionException e) {
				logger.severe("failed to load marker '" + label.getText() + "': " + e.getMessage());
			}
		}
	}
	
	/**
	 * 
	 * @return	an unmodifiable {@code Iterable} of {@code Marker}s
	 */
	public Iterable<Marker> getMarkers() {
		return ImmutableSet.copyOf(markers);
	}
	
	/**
	 * A marker on the world map.
	 * 
	 * @author mdriesen
	 */
	public static final class Marker {
		/** The x coordinate of the marker. */
		public final int x;
		/** The y coordinate of the marker. */
		public final int y;
		/** The text on the marker. */
		public final String text;
		
		/**
		 * The text must not be null.
		 * 
		 * @param x
		 * @param y
		 * @param text
		 */
		private Marker(int x, int y, String text) {
			this.text = Objects.requireNonNull(text, "text");
			this.x = x;
			this.y = y;
		}
	}
}
