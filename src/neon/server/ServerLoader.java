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
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import neon.system.files.NeonFileSystem;
import neon.system.resources.CClient;
import neon.system.resources.CGame;
import neon.system.resources.CServer;
import neon.system.resources.ConfigurationLoader;
import neon.system.resources.MissingLoaderException;
import neon.system.resources.ModuleLoader;
import neon.system.resources.RModule;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

public class ServerLoader {
	private static final Logger logger = Logger.getGlobal();

	/**
	 * Configures the file system and resource manager. Configuration data is
	 * added to the resource manager as server, client and game configuration 
	 * resources.
	 * 
	 * @param files
	 * @param manager
	 */
	public void configure(NeonFileSystem files, ResourceManager manager) {
		try {
			CServer configuration = initConfiguration();
			initFileSystem(files, configuration.getModules());
			initResources(manager, configuration);
			initClient(manager, configuration);
			initGame(manager, configuration);
		} catch (JDOMException e) {
			logger.severe("JDOMException in server configuration");
		} catch (IOException e) {
			logger.severe("could not load configuration file");
		}	
	}
	
	private void initGame(ResourceManager resources, CServer configuration) {
		HashSet<String> species = new HashSet<>();
		// default game title
		String title = "neon";
		
		// go through the loaded modules to check if any redefined the title
		for (String id : configuration.getModules()) {
			try {
				RModule module = resources.getResource("config", id);
				species.addAll(module.getSpecies());
				if (module.getTitle() != null) {
					title = module.getTitle();
				}
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
		
		// add client configuration resource to the manager
		try {
			CGame game = new CGame(title, species);			
			resources.addResource("config", game);
		} catch (MissingLoaderException e) {
			logger.severe(e.getMessage());
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}		
	}

	private CServer initConfiguration() throws IOException, JDOMException {
		// try to load the neon.ini file
		try (InputStream in = Files.newInputStream(Paths.get(".", "neon.ini"))) {
			Document doc = new SAXBuilder().build(in);
			return (CServer) new ConfigurationLoader().load(doc.getRootElement());
		} 	
	}
	
	private void initFileSystem(NeonFileSystem files, String[] modules) {
		try {
			// initialize file system with required modules and temp folder
			for (String module : modules) {
				files.addModule(module);
			}
			files.setTemporaryFolder(Paths.get(".", "temp"));
		} catch (IOException e) {
			logger.severe("could not initialize file system");			
		}
	}
	
	private void initResources(ResourceManager resources, CServer configuration) {
		// add all necessary resource loaders to the manager
		resources.addLoader("config", new ConfigurationLoader());
		ModuleLoader loader = new ModuleLoader();
		resources.addLoader("module", loader);
		
		// add server configuration resource to the manager
		try {
			resources.addResource("config", configuration);
		} catch (MissingLoaderException e) {
			logger.severe(e.getMessage());
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}

		// add module resources to the manager (most other resources will be loaded on demand)
		for (String id : configuration.getModules()) {
			try (InputStream in = Files.newInputStream(Paths.get("data", id, "main.xml"))) {
				Document doc = new SAXBuilder().build(in);
				RModule module = loader.load(doc.getRootElement());
				module.setID(id);
				resources.addResource("config", module);
			} catch (JDOMException e) {
				logger.severe("JDOMException in module configuration");
			} catch (IOException e) {
				logger.severe("error loading " + e.getMessage());
			} catch (MissingLoaderException e) {
				logger.severe(e.getMessage());
			}		
		}
	}
	
	private void initClient(ResourceManager resources, CServer configuration) {
		// add client configuration resource to the manager
		try {
			CClient client = new CClient();			
			resources.addResource("config", client);
		} catch (MissingLoaderException e) {
			logger.severe(e.getMessage());
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}
}
