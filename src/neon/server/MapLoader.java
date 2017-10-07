/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

package neon.server;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;

import neon.common.resources.RCreature;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ResourceLoader;
import neon.entity.entities.Creature;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
class MapLoader implements ResourceLoader<RMap> {
	private final EntityTracker tracker;
	private final ResourceManager resources;
	
	MapLoader(EntityTracker entities, ResourceManager resources) {
		tracker = entities;
		this.resources = resources;
	}
	
	public RMap load(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		RMap map = new RMap(id, name, width, height, uid);
		
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		initEntities(map, root.getChild("entities"));
		
		return map;
	}
	
	public Element save(RMap map) {
		Element root = new Element("map");
		root.setAttribute("id", map.id);
		root.setAttribute("name", map.name);
		root.setAttribute("uid", Short.toString(map.uid));
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(map.getWidth()));
		size.setAttribute("height", Integer.toString(map.getHeight()));
		root.addContent(size);
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		Map<Rectangle, String> leaves = map.getTerrain().getLeaves();
		for (Entry<Rectangle, String> entry : leaves.entrySet()) {
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
		
		Element height = new Element("height");
		root.addContent(height);
		
		Element entities = new Element("entities");
		root.addContent(entities);
		
		return root;
	}

	private void initTerrain(RMap map, Element terrain) {
		for (Element region : terrain.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			String id = region.getAttributeValue("id");
			
			map.getTerrain().add(new Rectangle(x, y, width, height), id);
		}
	}
	
	private void initElevation(RMap map, Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int value = Integer.parseInt(region.getAttributeValue("v"));
			
			map.getElevation().add(new Rectangle(x, y, width, height), value);
		}		
	}
	
	private void initEntities(RMap map, Element entities) {
		long mod = 0;	// TODO: juiste mod uid
		for (Element entity : entities.getChildren("creature")) {
			long uid = mod | Integer.parseInt(entity.getAttributeValue("uid"));
			map.getEntities().add(uid);

			try {
				RCreature rc = resources.getResource("creatures", entity.getAttributeValue("id"));
				Creature creature = new Creature(uid, rc);
				creature.shape.setX(Integer.parseInt(entity.getAttributeValue("x")));
				creature.shape.setY(Integer.parseInt(entity.getAttributeValue("y")));
				tracker.addEntity(creature);
			} catch (ResourceException e) {
				throw new IllegalStateException(e);
			}
		}		
	}
}
