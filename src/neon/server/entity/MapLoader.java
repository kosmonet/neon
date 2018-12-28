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

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import neon.common.entity.Entity;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.DoorInfo;
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
import neon.util.spatial.RegionSpatialIndex;

/**
 * A class to load maps.
 * 
 * @author mdriesen
 *
 */
public final class MapLoader {
	private static final Logger LOGGER = Logger.getGlobal();
	private static final XMLTranslator TRANSLATOR = new XMLTranslator();
	
	private final EntityManager entities;
	private final NeonFileSystem files;
	private final ResourceManager resources;
	
	/**
	 * Initializes a new map loader. The file system, resource manager and 
	 * entity manager must not be null.
	 * 
	 * @param files
	 * @param resources
	 * @param entities
	 */
	public MapLoader(NeonFileSystem files, ResourceManager resources, EntityManager entities) {
		this.files = Objects.requireNonNull(files, "file system");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		this.entities = Objects.requireNonNull(entities, "entity manager");
	}
	
	/**
	 * Loads a map.
	 * 
	 * @param id	the id of an {@code RMap}
	 * @return	a {@code Map}
	 * @throws IOException	if the map data is missing
	 * @throws ResourceException	if the map resource is missing
	 */
	public Map loadMap(String id) throws IOException, ResourceException {
		// load the map resource
		RMap resource = resources.getResource("maps", id);
		
		// load the map
		Element root = files.loadFile(TRANSLATOR, "maps", id + ".xml").getRootElement();
		Map map = new Map(resource, entities.getMapUID(resource.uid, resource.module));
		initTerrain(root.getChild("terrain"), map.getTerrain());
		initElevation(root.getChild("elevation"), map.getElevation());
		initEntities(root.getChild("entities"), map);
		
		// add map to the entity manager
		return map;
	}
	
	/**
	 * Initializes the terrain on a map.
	 * 
	 * @param terrain	a JDOM {@code Element} containing terrain data
	 * @param index	the {@code RegionSpatialIndex} of a map.
	 */
	private void initTerrain(Element terrain, RegionSpatialIndex<String> index) {
		for (Element region : terrain.getChildren("region")) {
			try {
				int width = region.getAttribute("w").getIntValue();
				int height = region.getAttribute("h").getIntValue();
				int x = region.getAttribute("x").getIntValue();
				int y = region.getAttribute("y").getIntValue();
				String id = region.getAttributeValue("id");
				index.insert(id, x, y, width, height);
			} catch (DataConversionException e) {
				LOGGER.severe("failed to load terrain: " + e.getMessage());
			}
		}
	}

	/**
	 * Initializes the height map of a map.
	 * 
	 * @param elevation
	 * @param index
	 */
	private void initElevation(Element elevation, RegionSpatialIndex<Integer> index) {
		for (Element region : elevation.getChildren("region")) {
			try {
				int width = region.getAttribute("w").getIntValue();
				int height = region.getAttribute("h").getIntValue();
				int x = region.getAttribute("x").getIntValue();
				int y = region.getAttribute("y").getIntValue();
				int z = region.getAttribute("z").getIntValue();
				index.insert(z, x, y, width, height);
			} catch (DataConversionException e) {
				LOGGER.severe("failed to load elevation: " + e.getMessage());
			}
		}		
	}
	
	/**
	 * Initializes all entities on a map.
	 * 
	 * @param entities
	 * @param map
	 */
	private void initEntities(Element entities, Map map) {
		long base = (long) map.getUID() << 32;
		
		// load creatures
		for (Element entity : entities.getChildren("creature")) {
			try {
				Entity creature = loadCreature(entity, base);
				registerEntity(entity, creature.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				LOGGER.severe("unknown creature on map " + map.getID() + ": " + entity.getAttributeValue("id"));
			} catch (DataConversionException e) {
				LOGGER.severe("error loading creature on map " + map.getID() + ": " + e.getMessage());
			}
		}
		
		// load items
		for (Element entity : entities.getChildren("item")) {
			try {
				Entity item = loadItem(entity, base);
				registerEntity(entity, item.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				LOGGER.severe("unknown item on map " + map.getID() + ": " + entity.getAttributeValue("id"));
			} catch (DataConversionException e) {
				LOGGER.severe("error loading item on map " + map.getID() + ": " + e.getMessage());				
			}
		}
	}
	
	/**
	 * Sets the position of an entity on the map. 
	 * 
	 * @param entity
	 * @param shape
	 * @param map
	 * @throws DataConversionException	if the entity data is invalid
	 */
	private void registerEntity(Element entity, Shape shape, Map map) throws DataConversionException {
		int x = entity.getAttribute("x").getIntValue();
		int y = entity.getAttribute("y").getIntValue();
		map.addEntity(shape.getEntity(), x, y);		
		shape.setX(x);
		shape.setY(y);
	}
	
	/**
	 * Loads a creature.
	 * 
	 * @param entity
	 * @param base
	 * @return
	 * @throws ResourceException	if the creature resource is missing
	 * @throws DataConversionException	if the creature data is invalid
	 */
	private Entity loadCreature(Element entity, long base) throws ResourceException, DataConversionException {
		// create a new creature
		long uid = base | entity.getAttribute("uid").getIntValue();
		RCreature rc = resources.getResource("creatures", entity.getAttributeValue("id"));
		Entity creature = entities.createEntity(uid, rc);
		
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
		
		return creature;
	}
	
	/**
	 * Loads an item.
	 * 
	 * @param entity
	 * @param base
	 * @return
	 * @throws ResourceException	if the item resource is missing
	 * @throws DataConversionException	if the item data is invalid
	 */
	private Entity loadItem(Element entity, long base) throws ResourceException, DataConversionException {
		// create a new item
		long uid = base | entity.getAttribute("uid").getIntValue();
		RItem ri = resources.getResource("items", entity.getAttributeValue("id"));
		Entity item = entities.createEntity(uid, ri);
		
		// check if item is a container
		if (item.hasComponent(Inventory.class)) {
			Inventory contents = item.getComponent(Inventory.class);
			for (Element child : entity.getChildren("item")) {
				contents.addItem(loadItem(child, base).uid);
			}
		}
		
		// check if item is a linked door
		if (entity.getChild("link") != null) {
			Element link = entity.getChild("link");
			int x = link.getAttribute("x").getIntValue();
			int y = link.getAttribute("y").getIntValue();
			String id = link.getAttributeValue("map");
			DoorInfo info = new DoorInfo(item.uid, id, link.getText(), x, y);
			item.setComponent(info);
		}

		return item;
	}
}
