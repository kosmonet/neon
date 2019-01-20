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

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
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
 * A class that takes care of loading and saving maps.
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
	 * @param files	the server file system
	 * @param resources	the server resource manager
	 * @param entities	the entity manager
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
		int uid = entities.getMapUID(resource.uid, resource.module);
		Map map = new Map(resource, uid);
		Element root;
		
		// check if this map was cached
		if (files.listFiles("maps").contains(Integer.toString(uid) + ".xml")) {
			// load the map from cache
			LOGGER.fine("loading map <" + uid + "> from temp folder");
			root = files.loadFile(TRANSLATOR, "maps", Integer.toString(uid) + ".xml").getRootElement();
			initSavedEntities(root.getChild("entities"), map);
		} else {
			// load the map from module
			LOGGER.fine("loading map <" + id + "> from module <" + resource.module + ">");
			root = files.loadFile(TRANSLATOR, "maps", id + ".xml").getRootElement();
			initEntities(root.getChild("entities"), map);			
		}

		initTerrain(root.getChild("terrain"), map.getTerrain());
		initElevation(root.getChild("elevation"), map.getElevation());
		initMarkers(root.getChild("labels"), map);
		
		// add map to the entity manager
		return map;
	}

	/**
	 * Saves a map to the temp folder on disk.
	 * 
	 * @param map	the {@code Map} to save.
	 */
	public void saveMap(Map map) {
		Element root = new Element("map");
		root.setAttribute("id", map.getId());
		root.setAttribute("uid", Integer.toString(map.getUid()));
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(map.getWidth()));
		size.setAttribute("height", Integer.toString(map.getHeight()));
		root.addContent(size);
		
		Element entities = new Element("entities");
		root.addContent(entities);
		for (Long uid : map.getEntities()) {
			Element entity = new Element("entity");
			entity.setAttribute("uid", Long.toString(uid));
			entities.addContent(entity);
		}
		
		Element labels = new Element("labels");
		root.addContent(labels);
		for (Element marker : map.getMarkers()) {
			labels.addContent(marker);
		}
		
		Element elevation = new Element("elevation");
		root.addContent(elevation);
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		for (Entry<Rectangle, String> entry : map.getTerrain().getElements().entrySet()) {
			if (entry.getValue() != null) {
				Element region = new Element("region");
				region.setAttribute("x", Integer.toString(entry.getKey().x));
				region.setAttribute("y", Integer.toString(entry.getKey().y));
				region.setAttribute("w", Integer.toString(entry.getKey().width));
				region.setAttribute("h", Integer.toString(entry.getKey().height));
				region.setAttribute("id", entry.getValue());
				terrain.addContent(region);
			}
		}
		
		try {
			files.saveFile(new Document(root), TRANSLATOR, "maps", map.getUid() + ".xml");
		} catch (IOException e) {
			LOGGER.severe("could not save map <" + map.getId() + ">");
		}
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
	 * @param elevation	the JDOM {@code Element} containing height data
	 * @param index	the spatial index containing height data
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
	 * Initializes the map markers.
	 * 
	 * @param labels	the JDOM {@code Element} containing markers
	 * @param map	the {@code Map}
	 */
	private void initMarkers(Element labels, Map map) {
		for (Element label : labels.getChildren()) {
			map.addMarker(label.detach());			
		}
	}
	
	/**
	 * Initializes all entities on a map.
	 * 
	 * @param entities	the JDOM {@code Element} containing entities
	 * @param map	the {@code Map}
	 */
	private void initEntities(Element entities, Map map) {
		long base = (long) map.getUid() << 32;
		
		// load creatures
		for (Element entity : entities.getChildren("creature")) {
			try {
				Entity creature = loadCreature(entity, base);
				registerEntity(entity, creature.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				LOGGER.severe("unknown creature on map " + map.getId() + ": " + entity.getAttributeValue("id"));
			} catch (DataConversionException e) {
				LOGGER.severe("error loading creature on map " + map.getId() + ": " + e.getMessage());
			}
		}
		
		// load items
		for (Element entity : entities.getChildren("item")) {
			try {
				Entity item = loadItem(entity, base);
				registerEntity(entity, item.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				LOGGER.severe("unknown item on map <" + map.getId() + ">: " + entity.getAttributeValue("id"));
			} catch (DataConversionException e) {
				LOGGER.severe("error loading item on map <" + map.getId() + ">: " + e.getMessage());				
			}
		}
	}
	
	/**
	 * Initializes all entities on a map.
	 * 
	 * @param element	the JDOM {@code Element} containing entities
	 * @param map	the {@code Map}
	 */
	private void initSavedEntities(Element element, Map map) {
		// load creatures
		for (Element entity : element.getChildren()) {
			try {
				long uid = entity.getAttribute("uid").getLongValue();
				Shape shape = entities.getEntity(uid).getComponent(Shape.class);
				map.addEntity(uid, shape.getX(), shape.getY());
			} catch (DataConversionException e) {
				LOGGER.severe("can't load entity <" + entity.getAttributeValue("uid") + ">");
			}
		}
	}
	
	/**
	 * Sets the position of an entity on the map. 
	 * 
	 * @param entity	the JDOM {@code Element} containing the entity data
	 * @param shape	the shape component of the entity
	 * @param map	the map
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
	 * @param entity	the JDOM {@code Element} containing the creature data
	 * @param base	the uid of the map, shifted 32 bits to the left
	 * @return	a creature {@code Entity}
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
	 * @param entity	the JDOM {@code Element} containing the item data
	 * @param base	the uid of the map, shifted 32 bits to the left
	 * @return	an item {@code Entity}
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
