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

package neon.server.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.LoadEvent;
import neon.common.event.UpdateEvent;
import neon.common.entity.Entity;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Lock;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Skills;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.InputEvent;
import neon.common.event.MessageEvent;
import neon.common.event.NewGameEvent;
import neon.common.files.FileUtils;
import neon.common.files.NeonFileSystem;
import neon.common.resources.CClient;
import neon.common.resources.CGame;
import neon.common.resources.CServer;
import neon.common.resources.RCreature;
import neon.common.resources.RMap;
import neon.common.resources.RModule;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ConfigurationLoader;
import neon.server.entity.EntityManager;
import neon.systems.ai.Behavior;
import neon.systems.combat.Armor;
import neon.systems.combat.Weapon;
import neon.systems.magic.Enchantment;
import neon.systems.magic.Magic;

/**
 * This class takes care of starting new games, loading old games and saving
 * games. 
 * 
 * @author mdriesen
 * 
 */
public final class GameLoader {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final ResourceManager resources;
	private final EntityManager entities;
	private final NeonFileSystem files;
	
	/**
	 * Initializes this game loader.
	 * 
	 * @param bus
	 * @param resources
	 * @param entities
	 */
	public GameLoader(NeonFileSystem files, ResourceManager resources, EntityManager entities, EventBus bus) {
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
	private void startNewGame(NewGameEvent.Check event) throws ResourceException, IOException {
		logger.info("starting a new game");

		if (isValidCharacter(event)) {
			// get the start map
			CServer config = resources.getResource("config", "server");
			CGame game = initGame(resources, config.getModules());
			resources.addResource(game);
			RMap map = resources.getResource("maps", game.map);

			// the player character
			RCreature species = resources.getResource("creatures", event.species);
			Entity player = entities.createEntity(PLAYER_UID, species);
			player.setComponent(new PlayerInfo(PLAYER_UID, event.name, event.gender));
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
			
			// tell the client everything is ready
			bus.post(new NewGameEvent.Pass());
			notifyClient(map);
		} else {
			bus.post(new NewGameEvent.Fail());			
		}
	}
	
	/**
	 * Checks whether the character described in the event is a valid character
	 * according to the game rules.
	 * 
	 * @param event
	 * @return
	 */
	private boolean isValidCharacter(NewGameEvent.Check event) {
		try {
			CClient config = resources.getResource("config", "client");			
			return config.getPlayableSpecies().contains(event.species);
		} catch (ResourceException e) {
			logger.severe("client configuration not found");
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
		ArrayList<String> items = new ArrayList<>();
		HashSet<String> spells = new HashSet<>();
		
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
				logger.warning("problem loading module " + id);
			}
		}
		
		CGame game = new CGame(map, x, y, money, items, spells);
		return game;
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
	private void startOldGame(LoadEvent.Start event) throws ResourceException, IOException {
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
		RMap map = resources.getResource("maps", game.map);

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
		Entity player = entities.getEntity(PLAYER_UID);
		bus.post(new UpdateEvent.Start());		
		notifyPlayer(player);

		// then send the map
		bus.post(new UpdateEvent.Map(map));

		for (long uid : map.getEntities()) {
			Entity entity = entities.getEntity(uid);
			Shape shape = entity.getComponent(Shape.class);
			if (entity.hasComponent(CreatureInfo.class)) {
				notifyCreature(entity);
				bus.post(new UpdateEvent.Move(uid, map.id, shape.getX(), shape.getY(), shape.getZ()));
			} else if (entity.hasComponent(ItemInfo.class)) {
				notifyItem(entity);
				bus.post(new UpdateEvent.Move(uid, map.id, shape.getX(), shape.getY(), shape.getZ()));
			}
		}		
	}
	
	private void notifyPlayer(Entity player) {
		Inventory inventory = player.getComponent(Inventory.class);
		inventory.getItems().parallelStream().forEach(uid -> notifyItem(entities.getEntity(uid)));
		bus.post(new ComponentUpdateEvent(inventory));
		bus.post(new ComponentUpdateEvent(player.getComponent(Stats.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(Skills.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(Magic.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(CreatureInfo.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(Graphics.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(Shape.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(PlayerInfo.class)));
		bus.post(new ComponentUpdateEvent(player.getComponent(Equipment.class)));
	}
	
	private void notifyCreature(Entity creature) {
		Inventory inventory = creature.getComponent(Inventory.class);
		inventory.getItems().parallelStream().forEach(uid -> notifyItem(entities.getEntity(uid)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Behavior.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(CreatureInfo.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Graphics.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Magic.class)));
		bus.post(new ComponentUpdateEvent(creature.getComponent(Equipment.class)));
	}
	
	/**
	 * Notifies the client of a new {@code Item}.
	 * 
	 * @param item
	 */
	private void notifyItem(Entity item) {
		bus.post(new ComponentUpdateEvent(item.getComponent(ItemInfo.class)));
		bus.post(new ComponentUpdateEvent(item.getComponent(Graphics.class)));
		
		if (item.hasComponent(Clothing.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Clothing.class)));
			if (item.hasComponent(Armor.class)) {
				bus.post(new ComponentUpdateEvent(item.getComponent(Armor.class)));
			}
		} else if (item.hasComponent(Weapon.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Weapon.class)));
		}
		
		if (item.hasComponent(Enchantment.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Enchantment.class)));
		}
		
		if (item.hasComponent(Lock.class)) {
			bus.post(new ComponentUpdateEvent(item.getComponent(Lock.class)));
		}
		
		if (item.hasComponent(Inventory.class)) {
			Inventory inventory = item.getComponent(Inventory.class);
			inventory.getItems().parallelStream().forEach(uid -> notifyItem(entities.getEntity(uid)));
			bus.post(new ComponentUpdateEvent(item.getComponent(Inventory.class)));
		}
	}
	
	/**
	 * Sends a list of all saved characters to the client.
	 * 
	 * @param event
	 */
	@Subscribe
	private void listSavedGames(LoadEvent.Load event) {
		Set<String> saves = FileUtils.listFiles(Paths.get("saves"));
		logger.info("saved characters: " + saves);
		bus.post(new LoadEvent.List(saves));
	}
	
	/**
	 * Saves the currently running game.
	 * 
	 * @param event
	 * @throws IOException
	 * @throws ResourceException 
	 */
	@Subscribe
	private void saveGame(InputEvent.Save event) throws IOException, ResourceException {
		logger.info("save game");
		// TODO: config en maps opslaan
		// store all cached entities
		entities.flush();
		// move the temp folder to the saves folder
		Entity player = entities.getEntity(PLAYER_UID);
		PlayerInfo record = player.getComponent(PlayerInfo.class);
		FileUtils.moveFolder(Paths.get("temp"), Paths.get("saves", record.getName()));
	}
}
