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
import java.util.LinkedHashSet;

import org.jdom2.Element;

/**
 * A resource loader for server, client and game configuration files.
 * 
 * @author mdriesen
 *
 */
public class ConfigurationLoader implements ResourceLoader {
	@Override
	public Resource load(Element root) {
		switch (root.getName()) {
		case "config":
		case "server":
			return loadServer(root);
		case "client":
			return loadClient(root);
		case "game":
			return loadGame(root);
		default:
			throw new IllegalArgumentException("argument is not a configuration resource");
		}
	}

	@Override
	public Element save(Resource resource) {
		if (resource instanceof CGame) {
			return saveGame((CGame)resource);
		} else if (resource instanceof CClient) {
			return saveClient((CClient)resource);
		} else if (resource instanceof CServer) {
			return saveServer((CServer)resource);
		} else {
			throw new IllegalArgumentException("argument is not a configuration resource");			
		}
	}	

	private CServer loadServer(Element root) {
		// LinkedHashSet to preserve module load order and to prevent doubles
		LinkedHashSet<String> modules = new LinkedHashSet<String>();
		for (Element module : root.getChild("modules").getChildren()) {
			modules.add(module.getText());
		}
		
		String level = root.getChildText("log").toUpperCase();
		return new CServer(modules, level);
	}


	private Element saveServer(Resource resource) {
		Element element = new Element("server");
		return element;
	}		

	private CClient loadClient(Element root) {
		return new CClient();
	}

	private Element saveClient(CClient resource) {
		Element element = new Element("client");
		return element;
	}

	private CGame loadGame(Element root) {
		String title = root.getAttributeValue("title");
		HashSet<String> species = new HashSet<>();
		
		Element playable = root.getChild("playable");
		if (playable != null) {
			for (Element id : playable.getChildren()) {
				species.add(id.getText());
			}
		}
		
		return new CGame(title, species);
	}

	private Element saveGame(CGame resource) {
		Element game = new Element("game");
		game.setAttribute("title", resource.getTitle());
		Element playable = new Element("playable");
		for (String id : resource.getPlayableSpecies()) {
			Element species = new Element("id");
			species.setText(id);
			playable.addContent(species);
		}
		game.addContent(playable);
		return game;
	}
}
