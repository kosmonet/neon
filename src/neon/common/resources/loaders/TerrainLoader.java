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

package neon.common.resources.loaders;

import org.jdom2.Element;

import javafx.scene.paint.Color;
import neon.common.resources.RTerrain;
import neon.util.GraphicsUtils;

/**
 * This resource loader takes care of loading and saving terrain resources 
 * from/to disk. 
 * 
 * @author mdriesen
 *
 */
public final class TerrainLoader implements ResourceLoader<RTerrain> {
	@Override
	public RTerrain load(Element root) {
		String id = root.getAttributeValue("id");
		char glyph = root.getChild("graphics").getAttributeValue("text").charAt(0);
		Color color = Color.web(root.getChild("graphics").getAttributeValue("color"));
		String name = root.getAttributeValue("name");
		RTerrain terrain = new RTerrain(id, name, glyph, color);
		
		for (Element modifier : root.getChildren("modifier")) {
			terrain.addModifier(RTerrain.Modifier.valueOf(modifier.getText().toUpperCase()));
		}
		
		return terrain;
	}

	@Override
	public Element save(RTerrain terrain) {
		Element root = new Element("terrain");
		root.setAttribute("id", terrain.id);
		root.setAttribute("name", terrain.name);
		
		Element graphics = new Element("graphics");
		graphics.setAttribute("text", Character.toString(terrain.glyph));
		graphics.setAttribute("color", GraphicsUtils.getColorString(terrain.color));
		root.addContent(graphics);
		
		for (RTerrain.Modifier modifier : terrain.getModifiers()) {
			Element mod = new Element("modifier");
			mod.setText(modifier.toString());
			root.addContent(mod);
		}
		
		return root;
	}
}
