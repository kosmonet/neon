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
import neon.common.resources.RCreature;
import neon.util.Graphics;

/**
 * A resource loader specifically for creatures.
 * 
 * @author mdriesen
 *
 */
public class CreatureLoader implements ResourceLoader<RCreature> {
	@Override
	public RCreature load(Element root) {
		String id = root.getAttributeValue("id");
		String name = root.getAttributeValue("name");
		String glyph = root.getChild("graphics").getAttributeValue("char");
		Color color = Color.web(root.getChild("graphics").getAttributeValue("color"));
		int speed = Integer.parseInt(root.getAttributeValue("speed"));
		RCreature creature = new RCreature(id, name, glyph, color, speed);
		return creature;
	}
	
	@Override
	public Element save(RCreature rc) {
		Element creature = new Element("creature");
		creature.setAttribute("id", rc.id);
		creature.setAttribute("name", rc.name);
		
		Element graphics = new Element("graphics");
		graphics.setAttribute("char", rc.glyph);
		graphics.setAttribute("color", Graphics.getColorString(rc.color));
		creature.addContent(graphics);
		
		return creature;
	}
}
