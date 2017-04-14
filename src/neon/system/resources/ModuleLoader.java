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

import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

public class ModuleLoader implements ResourceLoader {
	@Override
	public RModule load(Element root) {
		RModule module = new RModule(root.getAttributeValue("id"), root.getChildText("title"));
		
		Set<String> species = new HashSet<>();		
		Element playable = root.getChild("playable");
		for (Element id : playable.getChildren()) {
			species.add(id.getText());
		}
		module.addPlayableSpecies(species);
		
		return module;	
	}

	@Override
	public Element save(Resource resource) {
		RModule module = (RModule) resource;
		Element root = new Element("module");
		root.setAttribute("id", module.getID());
		Element title = new Element("title");
		title.setText(module.getTitle());
		root.addContent(title);
		Element playable = new Element("playable");
		root.addContent(playable);
		
		for (String species : module.getPlayableSpecies()) {
			Element id = new Element("id");
			id.setText(species);
			playable.addContent(id);
		}
		
		return root;
	}
}
