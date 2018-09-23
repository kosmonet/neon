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

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.jdom2.Element;

import neon.common.resources.CClient;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
import neon.common.resources.Resource;

/**
 * A resource loader for client and game configuration files.
 * 
 * @author mdriesen
 *
 */
public class ConfigurationLoader implements ResourceLoader<Resource> {
	@Override
	public Resource load(Element root) {
		switch (root.getName()) {
		case "client":
			return loadClient(root);
		case "game":
			return loadGame(root);
		case "server":
			return loadServer(root);
		default:
			throw new IllegalArgumentException("Argument is not a configuration resource");
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
			throw new IllegalArgumentException("Argument is not a configuration resource");			
		}
	}	

	public CServer loadServer(Element root) {
		// LinkedHashSet to preserve module load order and to prevent doubles
		LinkedHashSet<String> modules = new LinkedHashSet<String>();
		for (Element module : root.getChild("modules").getChildren()) {
			modules.add(module.getText());
		}
		
		String level = root.getChildText("log").toUpperCase();
		CServer cs = new CServer(modules, level);

		// extra step in case this was a saved configuration file
		if(root.getName().equals("server")) {
			for (Element module : root.getChild("modules").getChildren()) {
				cs.setModuleUID(module.getText(), Short.parseShort(module.getAttributeValue("uid")));
			}
		}

		return cs; 
	}

	private CClient loadClient(Element root) {
		String title = root.getAttributeValue("title");
		String subtitle = root.getAttributeValue("subtitle");
		String intro = root.getChildText("intro");
		HashSet<String> species = new HashSet<>();		
		return new CClient(title, subtitle, species, intro);
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
		
		Element log = new Element("log");
		log.setText(server.getLogLevel());
		root.addContent(log);
		
		return root;
	}		

	private Element saveClient(CClient config) {
		Element client = new Element("client");
		client.setAttribute("title", config.title);
		client.setAttribute("subtitle", config.subtitle);
		Element intro = new Element("intro");
		intro.setText(config.intro);
		client.addContent(intro);
		return client;
	}

	private CGame loadGame(Element root) {
		Element start = root.getChild("start");
		String map = start.getAttributeValue("map");
		return new CGame(map, -1, -1, -1);
	}

	private Element saveGame(CGame config) {
		Element game = new Element("game");
		Element start = new Element("start");
		start.setAttribute("map", config.getCurrentMap());
		game.addContent(start);
		return game;
	}
}
