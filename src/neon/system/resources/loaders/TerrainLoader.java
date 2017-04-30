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

import org.jdom2.Element;

import javafx.scene.paint.Color;
import neon.system.resources.RTerrain;

/**
 * This resource loader takes care of loading and saving terrain resources 
 * from/to disk. 
 * 
 * @author mdriesen
 *
 */
public class TerrainLoader implements ResourceLoader<RTerrain> {
	@Override
	public RTerrain load(Element root) {
		String id = root.getAttributeValue("id");
		String text = root.getAttributeValue("text");
		Color color = Color.web(root.getAttributeValue("color"));
		String name = root.getAttributeValue("name");
		return new RTerrain(id, name, text, color);
	}

	@Override
	public Element save(RTerrain terrain) {
		Element root = new Element("terrain");
		root.setAttribute("id", terrain.getID());
		root.setAttribute("name", terrain.getName());
		root.setAttribute("text", terrain.getText());
		root.setAttribute("color", terrain.getColorString());
		return root;
	}
}
