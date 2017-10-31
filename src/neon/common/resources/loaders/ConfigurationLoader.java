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

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.jdom2.Element;

import neon.common.resources.CClient;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
import neon.common.resources.Resource;

/**
 * A resource loader for server, client and game configuration files.
 * 
 * @author mdriesen
 *
 */
public class ConfigurationLoader implements ResourceLoader<Resource> {
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


	private Element saveServer(CServer server) {
		Element root = new Element("server");
		Element modules = new Element("modules");
		for(String id : server.getModules()) {
			Element module = new Element("module");
			module.setText(id);
			module.setAttribute("uid", Short.toString(server.getModuleUID(id)));
			modules.addContent(module);
		}
		
		root.addContent(modules);
		return root;
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
		
		Element start = root.getChild("start");
		String map = start.getAttributeValue("map");
		int x = Integer.parseInt(start.getAttributeValue("x"));
		int y = Integer.parseInt(start.getAttributeValue("y"));
		
		return new CGame(title, species, map, x, y);
	}

	private Element saveGame(CGame config) {
		Element game = new Element("game");
		game.setAttribute("title", config.title);
		
		Element start = new Element("start");
		start.setAttribute("map", config.getStartMap());
		start.setAttribute("x", Integer.toString(config.getStartX()));
		start.setAttribute("y", Integer.toString(config.getStartY()));
		game.addContent(start);
		
		return game;
	}
}
