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

package neon.server.resource;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.jdom2.Element;

import neon.common.resources.CServer;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ResourceLoader;
import neon.entity.components.CreatureInfo;
import neon.entity.components.Shape;
import neon.entity.entities.Entity;
import neon.server.entity.EntityManager;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
public class MapLoader implements ResourceLoader<RMap> {
	private static final Logger logger = Logger.getGlobal();
	
	private final EntityManager tracker;
	private final ResourceManager resources;
	private final CServer config;
	
	public MapLoader(EntityManager entities, ResourceManager resources, CServer config) {
		tracker = entities;
		this.resources = resources;
		this.config = config;
	}
	
	@Override
	public RMap load(Element root) {
		// check whether this is an original map from a module, or a map from a saved game
		if (root.getAttributeValue("module").equals("save")) {
			return loadFromSave(root);
		} else {
			return loadFromModule(root);
		}
	}
	
	/**
	 * Loads a map that comes straight from a module.
	 * 
	 * @param root
	 * @return
	 */
	private RMap loadFromModule(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		String module = root.getAttributeValue("module");
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		
		RMap map = new RMap(id, name, width, height, getMapUID(uid, module));
		
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		initEntitiesFromModule(map, root.getChild("entities"));
		
		return map;	
	}
	
	/**
	 * Loads a map that comes from a saved game.
	 * 
	 * @param root
	 * @return
	 */
	private RMap loadFromSave(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		int uid = Integer.parseInt(root.getAttributeValue("uid"));
		
		RMap map = new RMap(id, name, width, height, uid);
		
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		initEntitiesFromSave(map, root.getChild("entities"));
		
		return map;			
	}
	
	@Override
	public Element save(RMap map) {
		Element root = new Element("map");
		root.setAttribute("id", map.id);
		root.setAttribute("name", map.name);
		root.setAttribute("uid", Integer.toString(map.uid));
		root.setAttribute("module", "save");
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(map.getWidth()));
		size.setAttribute("height", Integer.toString(map.getHeight()));
		root.addContent(size);
		
		Element entities = new Element("entities");
		root.addContent(entities);
		for (long uid : map.getEntities()) {
			String type = "entity";
			
			if (tracker.getEntity(uid).hasComponent(CreatureInfo.class)) {
				type = "creature";
			}
			
			Element entity = new Element(type);
			entity.setAttribute("uid", Long.toString(uid));
			// we don't need to set the position, this is saved by the entity itself
			entities.addContent(entity);
		}
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		Map<Rectangle, String> terrainLeaves = map.getTerrain().getElements();
		for (Entry<Rectangle, String> entry : terrainLeaves.entrySet()) {
			if(entry.getValue() != null) {
				Element region = new Element("region");
				region.setAttribute("x", Integer.toString(entry.getKey().x));
				region.setAttribute("y", Integer.toString(entry.getKey().y));
				region.setAttribute("w", Integer.toString(entry.getKey().width));
				region.setAttribute("h", Integer.toString(entry.getKey().height));
				region.setAttribute("id", entry.getValue());
				terrain.addContent(region);
			}
		}
		
		Element elevation = new Element("elevation");
		root.addContent(elevation);
		Map<Rectangle, Integer> heightLeaves = map.getElevation().getElements();
		for (Entry<Rectangle, Integer> entry : heightLeaves.entrySet()) {
			if(entry.getValue() != null) {
				Element region = new Element("region");
				region.setAttribute("x", Integer.toString(entry.getKey().x));
				region.setAttribute("y", Integer.toString(entry.getKey().y));
				region.setAttribute("w", Integer.toString(entry.getKey().width));
				region.setAttribute("h", Integer.toString(entry.getKey().height));
				region.setAttribute("z", Integer.toString(entry.getValue()));
				terrain.addContent(region);
			}
		}
		
		return root;
	}

	private void initTerrain(RMap map, Element terrain) {
		for (Element region : terrain.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			String id = region.getAttributeValue("id");
			
			map.getTerrain().insert(new Rectangle(x, y, width, height), id);
		}
	}
	
	private void initElevation(RMap map, Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int value = Integer.parseInt(region.getAttributeValue("v"));
			
			map.getElevation().insert(new Rectangle(x, y, width, height), value);
		}		
	}
	
	private void initEntitiesFromModule(RMap map, Element entities) {
		long base = (long) map.uid << 32;
		
		// load creatures
		for (Element entity : entities.getChildren("creature")) {
			try {
				long uid = base | Integer.parseInt(entity.getAttributeValue("uid"));
				RCreature rc = resources.getResource("creatures", entity.getAttributeValue("id"));
				Entity creature = tracker.createEntity(uid, rc);
				initEntity(entity, creature.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				logger.severe("unknown creature on map " + map.id + ": " + entity.getAttributeValue("id"));
			}
		}
		
		// load items
		for (Element entity : entities.getChildren("item")) {
			try {
				long uid = base | Integer.parseInt(entity.getAttributeValue("uid"));
				RItem ri = resources.getResource("items", entity.getAttributeValue("id"));
				Entity item = tracker.createEntity(uid, ri);
				initEntity(entity, item.getComponent(Shape.class), map);
			} catch (ResourceException e) {
				logger.severe("unknown item on map " + map.id + ": " + entity.getAttributeValue("id"));
			}
		}
	}
	
	private void initEntity(Element entity, Shape shape, RMap map) {
		int x = Integer.parseInt(entity.getAttributeValue("x"));
		int y = Integer.parseInt(entity.getAttributeValue("y"));
		map.addEntity(shape.getEntity(), x, y);		
		shape.setX(x);
		shape.setY(y);
	}
	
	private void initEntitiesFromSave(RMap map, Element entities) {
		for (Element child : entities.getChildren()) {
			long uid = Integer.parseInt(child.getAttributeValue("uid"));
			Entity entity = tracker.getEntity(uid);
			Shape shape = entity.getComponent(Shape.class);
			map.addEntity(uid, shape.getX(), shape.getY());
		}
	}
	
	/**
	 * Calculates the full map uid given the module name and the base uid
	 * of the map within the module.
	 * 
	 * @param uid
	 * @param module
	 * @return
	 */
	private int getMapUID(short base, String module) {
		return ((int)config.getModuleUID(module) << 16) | base;
	}
}
