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
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Provider;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentUpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.conversation.Dialog;
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
	
	public Map loadMap(RMap resource) throws IOException {		
		Element root = files.loadFile(translator, "maps", resource.id + ".xml").getRootElement();
		Map map = new Map(resource, entities.getMapUID(resource.uid, resource.module));
		initTerrain(root.getChild("terrain"), map.getTerrain());
		initElevation(root.getChild("elevation"), map.getElevation());
		initEntities(root.getChild("entities"), map);
		return map;
	}
	
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
	
	private void registerEntity(Element entity, Shape shape, Map map) {
		int x = Integer.parseInt(entity.getAttributeValue("x"));
		int y = Integer.parseInt(entity.getAttributeValue("y"));
		map.addEntity(shape.getEntity(), x, y);		
		shape.setX(x);
		shape.setY(y);
	}
	
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
			short map = Short.parseShort(link.getAttributeValue("map"));
			DoorInfo info = new DoorInfo(item.uid, map, link.getText(), x, y);
			item.setComponent(info);
			bus.post(new ComponentUpdateEvent(info));
		}

		return item;
	}
}
