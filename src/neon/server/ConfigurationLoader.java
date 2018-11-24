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

package neon.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.google.common.io.Files;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.CClient;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
import neon.common.resources.Resource;
import neon.common.resources.loaders.ResourceLoader;
import neon.server.entity.EntityManager;

/**
 * A resource loader for client and game configuration files.
 * 
 * @author mdriesen
 *
 */
public final class ConfigurationLoader implements ResourceLoader {
	private static final String namespace = "config";
	
	private final XMLTranslator translator = new XMLTranslator();
	private final NeonFileSystem files;
	private final EntityManager entities;
	
	public ConfigurationLoader(NeonFileSystem files, EntityManager entities) {
		this.files = files;
		this.entities = entities;
	}
	
	@Override
	public Resource load(String id) throws IOException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
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
	public void save(Resource resource) throws IOException {
		if (resource.id.equals("game")) {
			Element root = saveGame(CGame.class.cast(resource));
			files.saveFile(new Document(root), translator, namespace, resource.id + ".xml");
		} else if (resource.id.equals("client")) {
			Element root = saveClient(CClient.class.cast(resource));
			files.saveFile(new Document(root), translator, namespace, resource.id + ".xml");
		} else if (resource.id.equals("server")) {
			Element root = saveServer(CServer.class.cast(resource));
			files.saveFile(new Document(root), translator, namespace, resource.id + ".xml");
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
		
		// set the correct module uid's in the entity manager
		if (root.getName().equals("server")) {
			// in case this was a saved configuration file
			for (Element module : root.getChild("modules").getChildren()) {
				entities.setModuleUID(module.getText(), Short.parseShort(module.getAttributeValue("uid")));
			}
		} else {
			// in case configuration was loaded from neon.ini
			short index = 1;
			for (String module : modules) {
				entities.setModuleUID(module, index++);
			}
		}

		String level = root.getChildText("log").toUpperCase();
		return new CServer(modules, level);
	}

	private CClient loadClient(Element root) {
		String title = root.getAttributeValue("title");
		String subtitle = root.getAttributeValue("subtitle");
		String intro = root.getChildText("intro");
		
		HashSet<String> playable = new HashSet<>();
		for (Element species : root.getChild("playable").getChildren()) {
			playable.add(species.getAttributeValue("id"));
		}
		
		return new CClient(title, subtitle, playable, intro);
	}

	private Element saveServer(CServer server) {
		Element root = new Element("server");

		Element modules = new Element("modules");
		for(String id : server.getModules()) {
			Element module = new Element("module");
			module.setText(id);
			module.setAttribute("uid", Short.toString(entities.getModuleUID(id)));
			modules.addContent(module);
		}
		root.addContent(modules);
		
		Element log = new Element("log");
		log.setText(server.getLogLevel().toString());
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
		
		Element playable = new Element("playable");
		client.addContent(playable);
		for (String id : config.getPlayableSpecies()) {
			Element species = new Element("species").setAttribute("id", id);
			playable.addContent(species);
		}
		
		return client;
	}

	private CGame loadGame(Element root) {
		Element start = root.getChild("start");
		String map = start.getAttributeValue("map");
		return new CGame(map, -1, -1, -1, Collections.emptyList(), Collections.emptySet());
	}

	private Element saveGame(CGame config) {
		Element game = new Element("game");
		Element start = new Element("start");
		start.setAttribute("map", config.map);
		game.addContent(start);
		return game;
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(namespace).parallelStream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) throws IOException {
		files.deleteFile(namespace, id + ".xml");
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
}
