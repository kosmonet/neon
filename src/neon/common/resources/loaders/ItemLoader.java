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

package neon.common.resources.loaders;

import org.jdom2.Element;

import javafx.scene.paint.Color;

import neon.common.resources.RItem;

/**
 * A resource loader specifically for items.
 * 
 * @author mdriesen
 *
 */
public class ItemLoader implements ResourceLoader<RItem> {
	@Override
	public RItem load(Element root) {
		String name = root.getAttributeValue("name");
		String id = root.getAttributeValue("id");
		String glyph = root.getChild("graphics").getAttributeValue("char");
		Color color = Color.web(root.getChild("graphics").getAttributeValue("color"));
		RItem item = new RItem(id, name, glyph, color);
		return item;
	}
	
	@Override
	public Element save(RItem ri) {
		Element item = new Element("item");
		item.setAttribute("id", ri.id);
		item.setAttribute("name", ri.name);
		return item;
	}
}
