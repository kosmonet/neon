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
import java.util.Objects;
import java.util.Set;
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
import neon.common.resources.loaders.CreatureLoader;
import neon.common.resources.loaders.ItemLoader;
import neon.common.resources.loaders.MapLoader;
import neon.common.resources.loaders.ModuleLoader;
import neon.common.resources.loaders.TerrainLoader;
import neon.server.entity.ClothingBuilder;
import neon.server.entity.CoinBuilder;
import neon.server.entity.ContainerBuilder;
import neon.server.entity.CreatureBuilder;
import neon.server.entity.DoorBuilder;
import neon.server.entity.EntityManager;
import neon.server.entity.ItemBuilder;
import neon.server.entity.Module;

/**
 * Most of the server configuration is performed by the {@code ServerLoader}.
 * 
 * @author mdriesen
 *
 */
final class ServerLoader {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final EventBus bus;
	
	/**
	 * Initializes this loader with an {@code EventBus}. The event bus must
	 * not be null.
	 * 
	 * @param bus
	 */
	ServerLoader(EventBus bus) {
		this.bus = Objects.requireNonNull(bus, "event bus");
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
			CServer configuration = initConfiguration(files, entities);
			LOGGER.setLevel(configuration.getLogLevel());
			initEntities(entities);
			initFileSystem(files, configuration.getModules());
			initResources(files, resources, configuration, entities);
			initClient(resources, configuration.getModules());
			LOGGER.info("server succesfully configured");
		} catch (JDOMException e) {
			LOGGER.severe("JDOMException in server configuration");
		} catch (IOException e) {
			LOGGER.severe("could not load configuration file");
		}
	}

	/**
	 * Loads the neon.ini file.
	 * 
	 * @param files
	 * @param entities
	 * @return
	 * @throws IOException	if the ini file is missing
	 * @throws JDOMException	if the ini file is corrupt
	 */
	private CServer initConfiguration(NeonFileSystem files, EntityManager entities) throws IOException, JDOMException {
		try (InputStream in = Files.newInputStream(Paths.get("neon.ini"))) {
			Document doc = new SAXBuilder().build(in);
			return new ConfigurationLoader(files, entities).loadServer(doc.getRootElement());
		} 
	}
	
	/**
	 * Adds some basic builders to the entity manager. Systems may add more
	 * builders later on.
	 * 
	 * @param entities
	 */
	private void initEntities(EntityManager entities) {
		entities.addBuilder(RItem.class, new ItemBuilder());
		entities.addBuilder(RItem.Clothing.class, new ClothingBuilder());
		entities.addBuilder(RCreature.class, new CreatureBuilder());
		entities.addBuilder(RItem.Coin.class, new CoinBuilder());
		entities.addBuilder(RItem.Container.class, new ContainerBuilder());
		entities.addBuilder(RItem.Door.class, new DoorBuilder());
	}
	
	/**
	 * Initializes the file system with required modules and temporary folder.
	 * 
	 * @param files
	 * @param modules
	 */
	private void initFileSystem(NeonFileSystem files, Set<String> modules) {
		try {
			for (String module : modules) {
				files.addModule(module);
			}
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			LOGGER.severe("could not initialize file system");
		}
	}
	
	/**
	 * Initializes the resource manager.
	 * 
	 * @param files
	 * @param resources
	 * @param configuration
	 * @param entities
	 */
	private void initResources(NeonFileSystem files, ResourceManager resources, CServer configuration, EntityManager entities) {
		// add all necessary resource loaders to the manager
		resources.addLoader(new ConfigurationLoader(files, entities));
		resources.addLoader(new ModuleLoader(files));
		resources.addLoader(new TerrainLoader(files));
		resources.addLoader(new CreatureLoader(files));
		resources.addLoader(new ItemLoader(files));
		resources.addLoader(new MapLoader(files));
		
		// check if all required parent modules are present
		try {
			Set<String> modules = new HashSet<>();
			for (String id : configuration.getModules()) {
				modules.add(id);
				RModule module = resources.getResource(id);				
				entities.addModule(new Module(module));
				for (String parent : module.getParentModules()) {
					if (!modules.contains(parent)) {
						LOGGER.warning("module <" + id + "> is missing parent <" + parent + ">");
						bus.post(new MessageEvent("Module <" + id + "> is missing parent <" + 
								parent + ">. Check if all necessary modules are present in "
								+ "the correct load order.", "Server configuration error"));
					}
				}
			}
		} catch (ResourceException e) {
			LOGGER.severe(e.getMessage());
		}
		
		// add server configuration resource to the manager
		try {
			resources.addResource(configuration);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	/**
	 * Creates the client configuration resource.
	 * 
	 * @param resources
	 * @param modules
	 */
	private void initClient(ResourceManager resources, Set<String> modules) {
		// use a set to prevent doubles
		Set<String> species = new HashSet<>();
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
				LOGGER.warning("problem loading module " + id);
			}
		}

		// add client configuration resource to the manager
		try {
			CClient client = new CClient(title, subtitle, species, intro);			
			resources.addResource(client);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
}
