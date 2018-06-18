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

package neon.server.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.ServerLoadEvent;
import neon.common.event.ClientLoadEvent;
import neon.common.event.MessageEvent;
import neon.common.event.NewGameEvent;
import neon.common.event.QuitEvent;
import neon.common.event.SaveEvent;
import neon.common.files.FileUtils;
import neon.common.files.NeonFileSystem;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.RModule;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ConfigurationLoader;
import neon.entity.components.InfoComponent;
import neon.entity.components.InventoryComponent;
import neon.entity.components.PlayerComponent;
import neon.entity.components.ShapeComponent;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Item;
import neon.entity.entities.Player;
import neon.entity.events.UpdateEvent;
import neon.server.EntityTracker;

/**
 * This class takes care of starting new games, loading old games and saving
 * games. 
 * 
 * @author mdriesen
 * 
 */
public class GameLoader {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	private final ResourceManager resources;
	private final EntityTracker entities;
	private final NeonFileSystem files;
	
	private Player player;
	
	/**
	 * Initializes this game loader.
	 * 
	 * @param bus
	 * @param resources
	 * @param entities
	 */
	public GameLoader(NeonFileSystem files, ResourceManager resources, EntityTracker entities, EventBus bus) {
		this.bus = bus;
		this.resources = resources;
		this.entities = entities;
		this.files = files;
	}
	
	/**
	 * Loads all data for a new game and sends this back to the client.
	 * 
	 * @param event
	 * @throws ResourceException
	 * @throws IOException 
	 */
	@Subscribe
	private void startNewGame(NewGameEvent event) throws ResourceException, IOException {
		logger.info("starting a new game");
		
		// get the start map
		CServer config = resources.getResource("config", "server");
		CGame game = initGame(resources, config.getModules());
		resources.addResource(game);
		RMap map = resources.getResource("maps", game.getStartMap());
		
		// the player character
		RCreature species = resources.getResource("creatures", event.getSpecies());
		player = new Player(event.getName(), event.getGender(), species);
		player.<ShapeComponent>getComponent("shape").setPosition(game.getStartX(), game.getStartY(), 0);
		entities.addEntity(player);
		
		entities.addEntity(new Item(6, resources.getResource("items", "cup")));
		entities.addEntity(new Item(2, resources.getResource("items", "cup_gold")));
		entities.addEntity(new Item(3, resources.getResource("items", "cup_silver")));
		entities.addEntity(new Item(4, resources.getResource("items", "cup_gold")));
		entities.addEntity(new Item(5, resources.getResource("items", "cup")));
		InventoryComponent inventory = player.getComponent("inventory");
		inventory.addItem(6);
		inventory.addItem(2);
		inventory.addItem(3);
		inventory.addItem(4);
		inventory.addItem(5);

		// tell the client everything is ready
		notifyClient(map);
	}

	/**
	 * Initializes the game resource. If an old game is loaded, the game 
	 * resource will be overwritten later by the one from the loaded game.
	 * 
	 * @param resources
	 * @param modules
	 */
	private CGame initGame(ResourceManager resources, String[] modules) {
		// default start position
		String map = "";
		int x = 0;
		int y = 0;
		
		// go through the loaded modules to check if any redefined the start position
		for (String id : modules) {
			try {
				RModule module = resources.getResource(id);
				map = module.getStartMap().isEmpty() ? map : module.getStartMap();
				x = (module.getStartX() >= 0) ? module.getStartX() : x;
				y = (module.getStartY() >= 0) ? module.getStartY() : y;
			} catch (ResourceException e) {
				// something went wrong loading the module, try to continue anyway
				logger.warning("problem loading module " + id);
			}
		}

		return new CGame(map, x, y);	
	}

	/**
	 * Loads all data from a previously saved game and sends this back to the 
	 * client.
	 * 
	 * @param event
	 * @throws ResourceException
	 * @throws IOException
	 */
	@Subscribe
	private void startOldGame(ServerLoadEvent.Start event) throws ResourceException, IOException {
		logger.info("loading save character <" + event.save + ">");
		
		// set the save folder in the file system
		try {
			files.setSaveFolder(Paths.get("saves", event.save));
		} catch (NotDirectoryException e) {
			logger.warning("<" + event.save + "> is not a valid saved game");
		}

		// load the server configuration file from the save folder and check each module uid
		CServer currentConfig = resources.getResource("config", "server");
		CServer oldConfig = getConfiguration(event.save);
		for (String module : oldConfig.getModules()) {
			if(currentConfig.hasModule(module)) {
				// make sure the current module uid is the same as in the saved game
				currentConfig.setModuleUID(module, oldConfig.getModuleUID(module));
			} else {
				// give a warning when a module was removed from the load order
				logger.warning("missing module <" + module + "> in saved game");
				bus.post(new MessageEvent("Module <" + module + "> is missing from the load order. "
						+ "This module was present when the game was originally saved. The game may "
						+ "behave in unexpected ways.", "Module warning"));
			}
		}
		// save all changes to the current configuration
		resources.addResource(currentConfig);
		
		// get the start map
		CGame game = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", game.getStartMap());

		// tell the client everything is ready
		notifyClient(map);
	}
	
	/**
	 * Loads the server configuration file from a previously saved game.
	 * 
	 * @param save
	 * @return
	 * @throws IOException
	 */
	private CServer getConfiguration(String save) throws IOException {
		// try to load the neon.ini file
		try (InputStream in = Files.newInputStream(Paths.get("saves", save, "config", "server.xml"))) {
			Document doc = new SAXBuilder().build(in);
			return new ConfigurationLoader().loadServer(doc.getRootElement());
		} catch (JDOMException e) {
			throw new IllegalStateException("failed to load server configuration file", e);
		}
	}

	/**
	 * Collects all necessary resources and sends them to the client.
	 * 
	 * @param map
	 * @throws ResourceException
	 */
	private void notifyClient(RMap map) throws ResourceException {
		// tell the client to start loading the map
		Player player = entities.getEntity(0);
		bus.post(new UpdateEvent.Start(player));

		// then send the map
		bus.post(new UpdateEvent.Map(map));

		// then everything else
		InventoryComponent inventory = player.getComponent("inventory");
		for (long uid : inventory.getItems()) {
			Item item = entities.getEntity(uid);
			InfoComponent<RItem> info = item.getComponent("info");			
			bus.post(new UpdateEvent.Item(uid, info.getResource().id, "", 0, 0, 0));
		}

		for (long uid : map.getEntities()) {
			Entity entity = entities.getEntity(uid);
			ShapeComponent shape = entity.getComponent("shape");
			InfoComponent<Resource> info = entity.getComponent("info");
			if (entity instanceof Creature) {
				bus.post(new UpdateEvent.Creature(uid, info.getResource().id, map.id, shape.getX(), shape.getY(), shape.getZ()));
			} else if (entity instanceof Item) {
				bus.post(new UpdateEvent.Item(uid, info.getResource().id, map.id, shape.getX(), shape.getY(), shape.getZ()));
			}
		}		
	}
	
	/**
	 * Sends a list of all saved characters to the client.
	 * 
	 * @param event
	 */
	@Subscribe
	private void listSavedGames(ServerLoadEvent.List event) {
		String[] saves = FileUtils.listFiles(Paths.get("saves"));
		logger.info("saved characters: " + Arrays.toString(saves));
		bus.post(new ClientLoadEvent.List(saves));
	}
	
	/**
	 * Saves the currently running game.
	 * 
	 * @param event
	 * @throws IOException
	 * @throws ResourceException 
	 */
	@Subscribe
	private void saveGame(SaveEvent event) throws IOException, ResourceException {
		logger.info("save game");
		// TODO: config en maps opslaan
		// store all cached entities
		entities.flush();
		// move the temp folder to the saves folder
		PlayerComponent record = player.getComponent("record");
		FileUtils.moveFolder(Paths.get("temp"), Paths.get("saves", record.getName()));
		// and request JavaFX to quit
		bus.post(new QuitEvent());
	}
}