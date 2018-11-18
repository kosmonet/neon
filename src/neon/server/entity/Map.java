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
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.jdom2.Element;

import neon.common.entity.Entity;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Provider;
import neon.common.entity.components.Shape;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.conversation.Dialog;
import neon.util.spatial.PointQuadTree;
import neon.util.spatial.PointSpatialIndex;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

public final class Map {
	private static final Logger logger = Logger.getGlobal();
	private static final XMLTranslator translator = new XMLTranslator();
	
	private final EntityManager tracker;
	private final ResourceManager resources;
	private final RMap map;
	private final RegionSpatialIndex<String> terrain;
	private final RegionSpatialIndex<Integer> elevation;
	private final PointSpatialIndex<Long> entities;
	private final int uid;

	public Map(RMap map, int uid, NeonFileSystem files, EntityManager tracker, ResourceManager resources) throws IOException {
		this.map = map;
		this.uid = tracker.getMapUID(map.uid, map.module);
		this.tracker = tracker;
		this.resources = resources;
		
		terrain = new RegionQuadTree<>(map.width, map.height);
		// initialize with a ground plane at 0 elevation
		elevation = new RegionQuadTree<>(map.width,  map.height, 0);
		entities = new PointQuadTree<>(0, 0, map.width, map.height, 100);
		
		Element root = files.loadFile(translator, "maps", map.id + ".xml").getRootElement();
		initTerrain(root.getChild("terrain"));
		initElevation(root.getChild("elevation"));
		initEntities(root.getChild("entities"));
	}
	
	public int getUid() {
		return uid;
	}
	
	public String getId() {
		return map.id;
	}
	
	public int getWidth() {
		return map.width;
	}
	
	public int getHeight() {
		return map.height;
	}
	
	public RegionSpatialIndex<String> getTerrain() {
		return terrain;
	}
	
	public Collection<Long> getEntities(int x, int y) {
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
	
	private void initEntities(Element entities) {
		long base = (long) uid << 32;
		
		// load creatures
		for (Element entity : entities.getChildren("creature")) {
			try {
				long uid = base | Integer.parseInt(entity.getAttributeValue("uid"));
				RCreature rc = resources.getResource("creatures", entity.getAttributeValue("id"));
				Entity creature = tracker.createEntity(uid, rc);
				
				// check if the creature has dialog
				if (entity.getAttribute("dialog") != null) {
					String dialog = entity.getAttributeValue("dialog");	
					creature.setComponent(new Dialog(uid, dialog));
				}
				
				// check if the creature provides any services
				if (!entity.getChildren("service").isEmpty()) {
					Provider services = new Provider(uid);
					for (Element service : entity.getChildren("service")) {
						services.addService(Provider.Service.valueOf(service.getAttributeValue("id").toUpperCase()));
					}
					creature.setComponent(services);
				}
				
				// check if the creature is member of any factions
				CreatureInfo info = creature.getComponent(CreatureInfo.class);
				for (Element faction : entity.getChildren("faction")) {
					info.addFaction(faction.getAttributeValue("id"));
				}
				
				initEntity(entity, creature.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				logger.severe("unknown creature on map " + map.id + ": " + entity.getAttributeValue("id"));
			}
		}
		
		// load items
		for (Element entity : entities.getChildren("item")) {
			try {
				Entity item = loadItem(entity, base);
				Inventory contents = item.getComponent(Inventory.class);
				for (Element child : entity.getChildren("item")) {
					contents.addItem(loadItem(child, base).uid);
				}
				
				initEntity(entity, item.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				logger.severe("unknown item on map " + map.id + ": " + entity.getAttributeValue("id"));
			}
		}
	}
	
	private void initEntity(Element entity, Shape shape, RMap map) {
		int x = Integer.parseInt(entity.getAttributeValue("x"));
		int y = Integer.parseInt(entity.getAttributeValue("y"));
		entities.insert(shape.getEntity(), x, y);		
		shape.setX(x);
		shape.setY(y);
	}
	
	private Entity loadItem(Element entity, long base) throws ResourceException {
		long uid = base | Integer.parseInt(entity.getAttributeValue("uid"));
		RItem item = resources.getResource("items", entity.getAttributeValue("id"));
		return tracker.createEntity(uid, item);
	}
}
