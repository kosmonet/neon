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

import neon.system.event.MessageEvent;
import neon.system.files.NeonFileSystem;
import neon.system.resources.CClient;
import neon.system.resources.CGame;
import neon.system.resources.CServer;
import neon.system.resources.RModule;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;
import neon.system.resources.loaders.ConfigurationLoader;
import neon.system.resources.loaders.MapLoader;
import neon.system.resources.loaders.ModuleLoader;
import neon.system.resources.loaders.TerrainLoader;

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
	 */
	void configure(NeonFileSystem files, ResourceManager manager) {
		try {
			CServer configuration = initConfiguration();
			logger.setLevel(Level.parse(configuration.getLogLevel()));
			initFileSystem(files, configuration.getModules());
			initResources(manager, configuration);
			initClient(manager);
			initGame(manager, configuration.getModules());
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
			return (CServer) new ConfigurationLoader().load(doc.getRootElement());
		} 	
	}
	
	/**
	 * Initializes the game resource. If an old game is loaded, the game 
	 * resource is overwritten by the one from the loaded game.
	 * 
	 * @param resources
	 * @param modules
	 */
	private void initGame(ResourceManager resources, String[] modules) {
		// use a set to prevent doubles
		HashSet<String> species = new HashSet<>();
		// default game title
		String title = "neon";
		// default start position
		String map = "";
		int x = 0;
		int y = 0;
		
		// go through the loaded modules to check if any redefined the title or start position
		for (String id : modules) {
			try {
				RModule module = resources.getResource(id);
				species.addAll(module.getPlayableSpecies());
				title = module.getTitle().isEmpty() ? title : module.getTitle();
				map = module.getStartMap().isEmpty() ? map : module.getStartMap();
				x = module.getStartX() > 0 ? module.getStartX() : x;
				y = module.getStartY() > 0 ? module.getStartY() : y;
			} catch (ResourceException e) {
				// something went wrong loading the module, try to continue anyway
				logger.warning("problem loading module " + id);
			}
		}
		
		// add game configuration resource to the manager
		try {
			CGame game = new CGame(title, species, map, x, y);			
			resources.addResource(game);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}		
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
	
	private void initResources(ResourceManager resources, CServer configuration) {
		// add all necessary resource loaders to the manager
		ConfigurationLoader loader = new ConfigurationLoader();
		resources.addLoader("config", loader);
		resources.addLoader("server", loader);
		resources.addLoader("client", loader);
		resources.addLoader("game", loader);
		resources.addLoader("module", new ModuleLoader());
		resources.addLoader("map", new MapLoader());
		resources.addLoader("terrain", new TerrainLoader());
		
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
								+ "the correct load order", "Server configuration error"));
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
	
	private void initClient(ResourceManager resources) {
		// add client configuration resource to the manager
		try {
			CClient client = new CClient();			
			resources.addResource(client);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}
}
