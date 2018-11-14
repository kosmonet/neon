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

import neon.common.resources.RModule;

/**
 * Responsible for loading and saving module resources.
 * 
 * @author mdriesen
 *
 */
public final class ModuleLoader implements ResourceLoader<RModule> {
	@Override
	public RModule load(Element root) {
		String id = root.getAttributeValue("id");
		String title = root.getChildText("title");
		String subtitle = root.getChildText("subtitle");
		String map = root.getChild("start").getAttributeValue("map");
		String intro = root.getChildren("intro").isEmpty() ? "" : root.getChildText("intro");
		Element start = root.getChild("start");
		int x = Integer.parseInt(start.getAttributeValue("x"));
		int y = Integer.parseInt(start.getAttributeValue("y"));		
		int money = Integer.parseInt(start.getAttributeValue("money"));
		
		RModule.Builder builder = new RModule.Builder(id).setTitle(title).setSubtitle(subtitle).setIntro(intro);
		builder.setStartMap(map).setStartMoney(money).setStartPosition(x, y);
		
		for (Element species : root.getChild("playable").getChildren()) {
			builder.addPlayableSpecies(species.getText());
		}
		
		for (Element parent : root.getChild("parents").getChildren()) {
			builder.addParentModule(parent.getText());
		}
		
		for (Element item : start.getChildren("item")) {
			builder.addStartItem(item.getAttributeValue("id"));
		}
		
		for (Element item : start.getChildren("spell")) {
			builder.addStartSpell(item.getAttributeValue("id"));
		}
		
		return builder.build();
	}

	@Override
	public Element save(RModule module) {
		Element root = new Element("module");
		root.setAttribute("id", module.id);
		Element title = new Element("title");
		title.setText(module.title);
		root.addContent(title);
		
		Element start = new Element("start");
		start.setAttribute("map", module.map);
		start.setAttribute("x", Integer.toString(module.x));
		start.setAttribute("y", Integer.toString(module.y));
		root.addContent(start);
		
		if (!module.intro.isEmpty()) {
			Element intro = new Element("intro");
			intro.setText(module.intro);
			root.addContent("intro");
		}
		
		Element playable = new Element("playable");
		root.addContent(playable);
		for (String species : module.getPlayableSpecies()) {
			Element id = new Element("id");
			id.setText(species);
			playable.addContent(id);
		}
		
		Element parents = new Element("parents");
		root.addContent(parents);
		for (String parent : module.getParentModules()) {
			Element id = new Element("id");
			id.setText(parent);
			parents.addContent(id);
		}
		
		return root;
	}
}
