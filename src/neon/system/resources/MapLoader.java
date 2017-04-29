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

package neon.system.resources;

import org.jdom2.Element;

public class MapLoader implements ResourceLoader<RMap> {
	@Override
	public RMap load(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		RMap map = new RMap(id, name);
		
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		map.setSize(width, height);
		
		return map;
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
		
		return root;
	}
}
