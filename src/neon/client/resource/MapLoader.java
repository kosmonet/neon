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

package neon.client.resource;

import java.awt.Rectangle;

import org.jdom2.Element;

import neon.common.resources.RMap;
import neon.common.resources.loaders.ResourceLoader;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
public final class MapLoader implements ResourceLoader<RMap> {
	@Override
	public RMap load(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		
		RMap map = new RMap(id, name, width, height, uid);
		
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		
		return map;	
	}
	
	@Override
	public Element save(RMap map) {
		throw new IllegalStateException("Client is not allowed to save resources.");
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
}
