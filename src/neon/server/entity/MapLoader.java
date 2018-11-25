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
import java.util.logging.Logger;

import org.jdom2.Element;

import com.google.common.eventbus.EventBus;

import neon.common.entity.Entity;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Lock;
import neon.common.entity.components.Provider;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.UpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.ai.Behavior;
import neon.systems.combat.Armor;
import neon.systems.combat.Weapon;
import neon.systems.conversation.Dialog;
import neon.systems.magic.Enchantment;
import neon.systems.magic.Magic;
import neon.util.spatial.RegionSpatialIndex;

public final class MapLoader {
	private static final Logger logger = Logger.getGlobal();
	
	private final XMLTranslator translator = new XMLTranslator();
	private final EntityManager entities;
	private final NeonFileSystem files;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public MapLoader(NeonFileSystem files, ResourceManager resources, EntityManager entities, EventBus bus) {
		this.files = files;
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
	}
	
	/**
	 * Loads a map.
	 * 
	 * @param id	the id of an {@code RMap}
	 * @return	a {@code Map}
	 * @throws IOException
	 * @throws ResourceException 
	 */
	public Map loadMap(String id) throws IOException, ResourceException {
		// load the map resource
		RMap resource = resources.getResource("maps", id);
		
		// load the map
		Element root = files.loadFile(translator, "maps", id + ".xml").getRootElement();
		Map map = new Map(resource, entities.getMapUID(resource.uid, resource.module));
		initTerrain(root.getChild("terrain"), map.getTerrain());
		initElevation(root.getChild("elevation"), map.getElevation());
		initEntities(root.getChild("entities"), map);
		
		// send all entities on the map to the client
		entities.addMap(map);
		notifyClient(map);		
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
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			String id = region.getAttributeValue("id");
			index.insert(id, x, y, width, height);
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
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int z = Integer.parseInt(region.getAttributeValue("z"));
			index.insert(z, x, y, width, height);
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
				logger.severe("unknown creature on map " + map.getID() + ": " + entity.getAttributeValue("id"));
			}
		}
		
		// load items
		for (Element entity : entities.getChildren("item")) {
			try {
				Entity item = loadItem(entity, base);
				registerEntity(entity, item.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				logger.severe("unknown item on map " + map.getID() + ": " + entity.getAttributeValue("id"));
			}
		}
	}
	
	/**
	 * Sets the position of an entity on the map. 
	 * 
	 * @param entity
	 * @param shape
	 * @param map
	 */
	private void registerEntity(Element entity, Shape shape, Map map) {
		int x = Integer.parseInt(entity.getAttributeValue("x"));
		int y = Integer.parseInt(entity.getAttributeValue("y"));
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
	 * @throws ResourceException
	 */
	private Entity loadCreature(Element entity, long base) throws ResourceException {
		// create a new creature
		long uid = base | Integer.parseInt(entity.getAttributeValue("uid"));
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
	 * @throws ResourceException
	 */
	private Entity loadItem(Element entity, long base) throws ResourceException {
		// create a new item
		long uid = base | Integer.parseInt(entity.getAttributeValue("uid"));
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
			int x = Integer.parseInt(link.getAttributeValue("x"));
			int y = Integer.parseInt(link.getAttributeValue("y"));
			String id = link.getAttributeValue("map");
			DoorInfo info = new DoorInfo(item.uid, id, link.getText(), x, y);
			item.setComponent(info);
			bus.post(new ComponentUpdateEvent(info));
		}

		return item;
	}
	
	/**
	 * Notifies the client that a new map was loaded.
	 * 
	 * @param map
	 * @throws ResourceException
	 */
	public void notifyClient(Map map) {
		// then send the map
		bus.post(new UpdateEvent.Map(map.getUID(), map.getID()));

		for (long uid : map.getEntities()) {
			Entity entity = entities.getEntity(uid);
			Shape shape = entity.getComponent(Shape.class);
			if (entity.hasComponent(CreatureInfo.class)) {
				notifyCreature(entity);
				bus.post(new UpdateEvent.Move(uid, map.getUID(), shape.getX(), shape.getY(), shape.getZ()));
			} else if (entity.hasComponent(ItemInfo.class)) {
				notifyItem(entity);
				bus.post(new UpdateEvent.Move(uid, map.getUID(), shape.getX(), shape.getY(), shape.getZ()));
			}
		}		
	}
	
	/**
	 * Notifies the client of a new creature.
	 * 
	 * @param creature
	 */
	private void notifyCreature(Entity creature) {
		Inventory inventory = creature.getComponent(Inventory.class);
		inventory.getItems().parallelStream().forEach(uid -> notifyItem(entities.getEntity(uid)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Behavior.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(CreatureInfo.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Graphics.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Magic.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Equipment.class)));
		if (creature.hasComponent(Provider.class)) {
			bus.post(new ComponentUpdateEvent(creature.getComponent(Provider.class)));			
		}
	}
	
	/**
	 * Notifies the client of a new item.
	 * 
	 * @param item
	 */
	public void notifyItem(Entity item) {
		bus.post(new ComponentUpdateEvent(item.getComponent(ItemInfo.class)));
		bus.post(new ComponentUpdateEvent(item.getComponent(Graphics.class)));
		
		if (item.hasComponent(Clothing.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Clothing.class)));
			if (item.hasComponent(Armor.class)) {
				bus.post(new ComponentUpdateEvent(item.getComponent(Armor.class)));
			}
		} else if (item.hasComponent(Weapon.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Weapon.class)));
		}
		
		if (item.hasComponent(Enchantment.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Enchantment.class)));
		}
		
		if (item.hasComponent(Lock.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Lock.class)));
		}
		
		if (item.hasComponent(DoorInfo.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(DoorInfo.class)));
		}
		
		if (item.hasComponent(Inventory.class)) {
			Inventory inventory = item.getComponent(Inventory.class);
			inventory.getItems().parallelStream().forEach(uid -> notifyItem(entities.getEntity(uid)));
			bus.post(new ComponentUpdateEvent(item.getComponent(Inventory.class)));
		}
	}
}
