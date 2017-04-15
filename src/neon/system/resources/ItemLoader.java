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

/**
 * A resource loader specifically for items.
 * 
 * @author mdriesen
 *
 */
public class ItemLoader implements ResourceLoader<RItem> {
	@Override
	public RItem load(Element root) {
		RItem creature = new RItem(root.getAttributeValue("id"), root.getName());
		creature.setName(root.getAttributeValue("name"));
		return creature;
	}
	
	@Override
	public Element save(RItem ri) {
		Element item = new Element(ri.getType());
		item.setAttribute("id", ri.getID());
		item.setAttribute("name", ri.getName());
		return item;
	}
}
