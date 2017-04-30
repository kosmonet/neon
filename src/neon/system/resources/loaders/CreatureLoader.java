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

import neon.system.resources.RCreature;

/**
 * A resource loader specifically for creatures.
 * 
 * @author mdriesen
 *
 */
public class CreatureLoader implements ResourceLoader<RCreature> {
	@Override
	public RCreature load(Element root) {
		RCreature creature = new RCreature(root.getAttributeValue("id"), root.getAttributeValue("name"));
		return creature;
	}
	
	@Override
	public Element save(RCreature rc) {
		Element creature = new Element(rc.getType());
		creature.setAttribute("id", rc.getID());
		creature.setAttribute("name", rc.getName());
		return creature;
	}
}
