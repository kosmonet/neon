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

package neon.features.magic;

import org.jdom2.Element;

import neon.common.resources.loaders.ResourceLoader;

public class SpellLoader implements ResourceLoader<RSpell> {
	@Override
	public RSpell load(Element root) {
		String id = root.getAttributeValue("id");
		Effect effect = Effect.valueOf(root.getAttributeValue("effect").toUpperCase());
		Target target = Target.valueOf(root.getAttributeValue("target").toUpperCase());
		int duration = Integer.parseInt(root.getAttributeValue("duration"));
		int magnitude = Integer.parseInt(root.getAttributeValue("magnitude"));
		
		RSpell spell = new RSpell(id, effect, target, duration, magnitude);
		return spell;
	}

	@Override
	public Element save(RSpell resource) {
		Element spell = new Element("spell");
		return spell;
	}
}
