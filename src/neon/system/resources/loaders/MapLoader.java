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

package neon.system.resources.loaders;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;

import neon.system.resources.RMap;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
public class MapLoader implements ResourceLoader<RMap> {
	@Override
	public RMap load(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		RMap map = new RMap(id, name, width, height);
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		
		return map;
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
		// initialize with a ground plane at 0 height
		map.getElevation().add(new Rectangle(0, 0, map.getWidth(), map.getHeight()), 0);
		
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int value = Integer.parseInt(region.getAttributeValue("v"));
			
			map.getElevation().add(new Rectangle(x, y, width, height), value);
		}		
	}

	@Override
	public Element save(RMap map) {
		Element root = new Element("map");
		root.setAttribute("id", map.getID());
		root.setAttribute("name", map.getName());
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(map.getWidth()));
		size.setAttribute("height", Integer.toString(map.getHeight()));
		root.addContent(size);
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		Map<Rectangle, String> leaves = map.getTerrain().getLeaves();
		for (Entry<Rectangle, String> entry : leaves.entrySet()) {
			Element region = new Element("region");
			region.setAttribute("x", Integer.toString(entry.getKey().x));
			region.setAttribute("y", Integer.toString(entry.getKey().y));
			region.setAttribute("w", Integer.toString(entry.getKey().width));
			region.setAttribute("h", Integer.toString(entry.getKey().height));
			region.setAttribute("id", entry.getValue());
			terrain.addContent(region);
		}
		
		Element height = new Element("height");
		root.addContent(height);
		
		return root;
	}
}
