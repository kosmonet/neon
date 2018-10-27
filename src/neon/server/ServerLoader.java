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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.common.eventbus.EventBus;

import neon.common.event.MessageEvent;
import neon.common.files.NeonFileSystem;
import neon.common.resources.CClient;
import neon.common.resources.CServer;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RModule;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ConfigurationLoader;
import neon.common.resources.loaders.CreatureLoader;
import neon.common.resources.loaders.DialogLoader;
import neon.common.resources.loaders.ItemLoader;
import neon.common.resources.loaders.ModuleLoader;
import neon.common.resources.loaders.TerrainLoader;
import neon.server.entity.ClothingBuilder;
import neon.server.entity.CreatureBuilder;
import neon.server.entity.EntityManager;
import neon.server.entity.ItemBuilder;
import neon.server.resource.MapLoader;

/**
 * Most of the server configuration is performed by the {@code ServerLoader}.
 * 
 * @author mdriesen
 *
 */
class ServerLoader {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	
	/**
	 * Initializes this loader with an {@code EventBus}.
	 * 
	 * @param bus
	 */
	ServerLoader(EventBus bus) {
		this.bus = bus;
	}

	/**
	 * Configures the file system and resource manager. Configuration data is
	 * added to the resource manager as server, client and game configuration 
	 * resources.
	 * 
	 * @param files
	 * @param manager
	 * @param entities
	 */
	void configure(NeonFileSystem files, ResourceManager resources, EntityManager entities) {
		try {
			CServer configuration = initConfiguration();
			logger.setLevel(Level.parse(configuration.getLogLevel()));
			initEntities(entities);
			initFileSystem(files, configuration.getModules());
			initResources(resources, configuration, entities);
			initClient(resources, configuration.getModules());
			logger.info("server succesfully configured");
		} catch (JDOMException e) {
			logger.severe("JDOMException in server configuration");
		} catch (IOException e) {
			logger.severe("could not load configuration file");
		}
	}

	private CServer initConfiguration() throws IOException, JDOMException {
		// try to load the neon.ini file
		try (InputStream in = Files.newInputStream(Paths.get("neon.ini"))) {
			Document doc = new SAXBuilder().build(in);
			return new ConfigurationLoader().loadServer(doc.getRootElement());
		} 	
	}
	
	private void initEntities(EntityManager entities) {
		// add all builders to the entity manager
		entities.addBuilder(RItem.class, new ItemBuilder());
		entities.addBuilder(RItem.Clothing.class, new ClothingBuilder());
		entities.addBuilder(RCreature.class, new CreatureBuilder());
	}
	
	/**
	 * Initializes the file system with required modules and temporary folder.
	 * 
	 * @param files
	 * @param modules
	 */
	private void initFileSystem(NeonFileSystem files, String[] modules) {
		try {
			for (String module : modules) {
				files.addModule(module);
			}
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			logger.severe("could not initialize file system");
		}
	}
	
	private void initResources(ResourceManager resources, CServer configuration, EntityManager entities) {
		// add all necessary resource loaders to the manager
		resources.addLoader("config", new ConfigurationLoader());
		resources.addLoader("global", new ModuleLoader());
		resources.addLoader("terrain", new TerrainLoader());
		resources.addLoader("creatures", new CreatureLoader());
		resources.addLoader("items", new ItemLoader());
		resources.addLoader("dialog", new DialogLoader());
		resources.addLoader("maps", new MapLoader(entities, resources, configuration));
		
		// check if all required parent modules are present
		try {
			HashSet<String> modules = new HashSet<>();
			for (String id : configuration.getModules()) {
				modules.add(id);
				RModule module = resources.getResource(id);
				for (String parent : module.getParents()) {
					if (!modules.contains(parent)) {
						logger.warning("module <" + id + "> is missing parent <" + parent + ">");
						bus.post(new MessageEvent("Module <" + id + "> is missing parent <" + 
								parent + ">. Check if all necessary modules are present in "
								+ "the correct load order.", "Server configuration error"));
					}
				}
			}
		} catch (ResourceException e) {
			logger.severe(e.getMessage());
		}
		
		// add server configuration resource to the manager
		try {
			resources.addResource(configuration);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}
	
	private void initClient(ResourceManager resources, String[] modules) {
		// use a set to prevent doubles
		HashSet<String> species = new HashSet<>();
		// default game title
		String title = "neon";
		String subtitle = "";
		String intro = "";
		
		// go through the loaded modules to check if any redefined the title or added playable species
		for (String id : modules) {
			try {
				RModule module = resources.getResource(id);
				species.addAll(module.getPlayableSpecies());
				title = module.title.isEmpty() ? title : module.title;
				subtitle = module.subtitle.isEmpty() ? subtitle : module.subtitle;
				intro = module.intro.isEmpty() ? intro : module.intro;
			} catch (ResourceException e) {
				// something went wrong loading the module, try to continue anyway
				logger.warning("problem loading module " + id);
			}
		}

		// add client configuration resource to the manager
		try {
			CClient client = new CClient(title, subtitle, species, intro);			
			resources.addResource(client);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}
}
