/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.LoadEvent;
import neon.common.event.UpdateEvent;
import neon.common.entity.Entity;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
import neon.common.event.MessageEvent;
import neon.common.event.NewGameEvent;
import neon.common.files.FileUtils;
import neon.common.files.NeonFileSystem;
import neon.common.resources.CClient;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
import neon.common.resources.RCreature;
import neon.common.resources.RModule;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.Configuration;
import neon.server.entity.EntityManager;
import neon.systems.magic.Magic;

/**
 * This class takes care of starting new games and loading old games. 
 * 
 * @author mdriesen
 */
public final class GameLoader {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final EventBus bus;
	private final ResourceManager resources;
	private final EntityManager entities;
	private final NeonFileSystem files;
	private final Notifier notifier;
	
	/**
	 * Initializes this game loader. The file system, resource manager, entity 
	 * manager and event bus must not be null.
	 * 
	 * @param files
	 * @param resources
	 * @param entities
	 * @param bus
	 */
	public GameLoader(NeonFileSystem files, ResourceManager resources, EntityManager entities, EventBus bus) {
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		this.entities = Objects.requireNonNull(entities, "entity manager");
		this.files = Objects.requireNonNull(files, "file system");
		notifier = new Notifier(entities, bus);
	}
	
	/**
	 * Loads all data for a new game and sends this back to the client.
	 * 
	 * @param event
	 * @throws ResourceException	if game resources are missing
	 * @throws IOException	if the game configuration can't be stored
	 */
	@Subscribe
	private void onNewGame(NewGameEvent.Check event) throws ResourceException, IOException {
		LOGGER.info("starting a new game");

		if (isValidCharacter(event)) {
			// get the start map
			CServer config = resources.getResource("config", "server");
			CGame game = initGame(resources, config.getModules());
			resources.addResource(game);

			// the player character
			RCreature species = resources.getResource("creatures", event.species);
			Entity player = entities.createEntity(Configuration.PLAYER_UID, species);
			player.setComponent(new PlayerInfo(Configuration.PLAYER_UID, event.name, event.gender));
			player.getComponent(Shape.class).setPosition(game.startX, game.startY, 0);

			Stats stats = player.getComponent(Stats.class);
			stats.setBaseCha(event.charisma);
			stats.setBaseCon(event.constitution);
			stats.setBaseDex(event.dexterity);
			stats.setBaseStr(event.strength);
			stats.setBaseWis(event.wisdom);
			stats.setBaseInt(event.intelligence);

			Inventory inventory = player.getComponent(Inventory.class);
			inventory.addMoney(game.startMoney);
			for (String id : game.getStartItems()) {
				long uid = entities.getFreeUID();
				entities.createEntity(uid, resources.getResource("items", id));
				inventory.addItem(uid);
			}

			Magic magic = player.getComponent(Magic.class);
			for (String id : game.getStartSpells()) {
				magic.addSpell(id);
			}
			
			// tell the client that character creation succeeded
			bus.post(new NewGameEvent.Pass());
			// send the new player character to the client
			notifier.notifyClient(player);
			// send the starting map to the client
			notifier.notifyClient(entities.getMap(game.map));
			// tell the client everything is ready to start
			bus.post(new UpdateEvent.Start());
		} else {
			bus.post(new NewGameEvent.Fail());			
		}
	}
	
	/**
	 * Checks whether the character described in the event is a valid character
	 * according to the game rules.
	 * 
	 * @param event
	 * @return	{@code true} if the list of playable species contains the character species, {@code false} otherwise
	 */
	private boolean isValidCharacter(NewGameEvent.Check event) {
		try {
			CClient config = resources.getResource("config", "client");			
			return config.getPlayableSpecies().contains(event.species);
		} catch (ResourceException e) {
			LOGGER.severe("client configuration not found");
			return false;
		}
	}

	/**
	 * Initializes the game resource. If an old game is loaded, the game 
	 * resource will be overwritten later by the one from the loaded game.
	 * 
	 * @param resources
	 * @param modules
	 */
	private CGame initGame(ResourceManager resources, Set<String> modules) {
		// defaults
		String map = "";
		int x = 0;
		int y = 0;
		int money = 0;
		int time = 0;
		List<String> items = new ArrayList<>();
		Set<String> spells = new HashSet<>();
		
		// go through the loaded modules to check if any redefined anything
		for (String id : modules) {
			try {
				RModule module = resources.getResource(id);
				map = module.map.isEmpty() ? map : module.map;
				x = (module.x >= 0) ? module.x : x;
				y = (module.y >= 0) ? module.y : y;
				money = (module.money >= 0) ? module.money : money;
				items.addAll(module.getStartItems());
				spells.addAll(module.getStartSpells());
			} catch (ResourceException e) {
				// something went wrong loading the module, try to continue anyway
				LOGGER.warning("problem loading module " + id);
			}
		}
		
		CGame game = new CGame(map, x, y, time, money, items, spells);
		return game;
	}

	/**
	 * Loads all data from a previously saved game and sends this back to the 
	 * client.
	 * 
	 * @param event
	 * @throws ResourceException	if game resources are missing
	 * @throws IOException	if the save folder is missing
	 */
	@Subscribe
	private void onLoadGame(LoadEvent.Start event) throws ResourceException, IOException {
		LOGGER.info("loading save character <" + event.save + ">");
		
		// set the save folder in the file system
		try {
			files.setSaveFolder(Paths.get("saves", event.save));
		} catch (NotDirectoryException e) {
			LOGGER.warning("<" + event.save + "> is not a valid saved game");
		}

		// load the server configuration file from the save folder and check each module uid
		CServer config = resources.getResource("config", "server");
		for (Entry<String, Short> entry : getConfiguration(event.save).entrySet()) {
			if (config.hasModule(entry.getKey())) {
				// make sure the current module uid is the same as in the saved game
				entities.setModuleUID(entry.getKey(), entry.getValue());
			} else {
				// give a warning when a module was removed from the load order
				LOGGER.warning("missing module <" + entry.getKey() + "> in saved game");
				bus.post(new MessageEvent("Module <" + entry.getKey() + "> is missing from the load order. "
						+ "This module was present when the game was originally saved. The game may "
						+ "behave in unexpected ways.", "Module warning"));
			}
		}
		
		// send the player to the client
		notifier.notifyClient(entities.getEntity(Configuration.PLAYER_UID));
		// get the start map
		CGame game = resources.getResource("config", "game");
		notifier.notifyClient(entities.getMap(game.map));
		// tell the client everything is ready to start
		bus.post(new UpdateEvent.Start(game.time));
	}
	
	/**
	 * Loads the server configuration file from a previously saved game.
	 * 
	 * @param save	the name of the saved character
	 * @return	a {@code Map<String, Short>} with all the module id's and uid's
	 * @throws IOException	if the configuration file can't be loaded
	 */
	private Map<String, Short> getConfiguration(String save) throws IOException {
		// try to load the neon.ini file
		try (InputStream in = Files.newInputStream(Paths.get("saves", save, "config", "server.xml"))) {
			Map<String, Short> modules = new HashMap<>();
			Document doc = new SAXBuilder().build(in);
			for (Element module : doc.getRootElement().getChild("modules").getChildren()) {
				modules.put(module.getText(), Short.parseShort(module.getAttributeValue("uid")));
			}
			
			return modules;
		} catch (JDOMException e) {
			throw new IllegalStateException("failed to load server configuration file", e);
		}
	}

	/**
	 * Posts a list of all saved characters on the event bus.
	 * 
	 * @param event
	 */
	@Subscribe
	private void listSavedGames(LoadEvent.Load event) {
		Set<String> saves = FileUtils.listFiles(Paths.get("saves"));
		LOGGER.info("saved characters: " + saves);
		bus.post(new LoadEvent.List(saves));
	}
}
