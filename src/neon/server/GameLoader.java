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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;

import neon.common.event.ServerLoadEvent;
import neon.common.event.ClientLoadEvent;
import neon.common.event.NewGameEvent;
import neon.common.event.QuitEvent;
import neon.common.event.SaveEvent;
import neon.common.files.FileUtils;
import neon.common.resources.CGame;
import neon.common.resources.RCreature;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.entities.Entity;
import neon.entity.entities.Item;
import neon.entity.entities.Player;
import neon.entity.events.UpdateEvent;

/**
 * This class takes care of starting new games, loading old games and saving
 * games. 
 * 
 * @author mdriesen
 *
 */
class GameLoader {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	private final ResourceManager resources;
	private final EntityTracker entities;
	
	private Player player;
	
	/**
	 * Initializes this game loader.
	 * 
	 * @param bus
	 * @param resources
	 * @param entities
	 */
	GameLoader(ResourceManager resources, EntityTracker entities, EventBus bus) {
		this.bus = bus;
		this.resources = resources;
		this.entities = entities;
	}
	
	/**
	 * Loads all data for a new game and sends this back to the client.
	 * 
	 * @param event
	 */
	@Subscribe
	private void startNewGame(NewGameEvent event) throws ResourceException {
		logger.info("starting a new game");
		
		// get the start map
		CGame game = resources.getResource("config", "game");
		game.setCurrentMap(game.getStartMap());
		RServerMap map = resources.getResource("maps", game.getStartMap());
		
		// the player character
		RCreature species = resources.getResource("creatures", event.getSpecies());
		player = new Player(event.getName(), event.getGender(), species);
		player.shape.setPosition(game.getStartX(), game.getStartY(), 0);
		entities.addEntity(player);
		
		entities.addEntity(new Item(6, resources.getResource("items", "cup")));
		entities.addEntity(new Item(2, resources.getResource("items", "cup_gold")));
		entities.addEntity(new Item(3, resources.getResource("items", "cup_silver")));
		entities.addEntity(new Item(4, resources.getResource("items", "cup_gold")));
		entities.addEntity(new Item(5, resources.getResource("items", "cup")));
		player.inventory.addItem(6);
		player.inventory.addItem(2);
		player.inventory.addItem(3);
		player.inventory.addItem(4);
		player.inventory.addItem(5);

		// collect all necessary resources to start the game
		Set<Resource> clientResources = new HashSet<>();
		clientResources.add(resources.getResource("config", "game"));
		clientResources.add(map);
		
		// add all terrain resources
		for (String terrain : map.getTerrain().getLeaves().values()) {
			clientResources.add(resources.getResource("terrain", terrain));
		}
		
		// collect all the necessary entities
		Set<Entity> clientEntities = new HashSet<>();
		clientEntities.add(entities.getEntity(0));

		for (Long uid : map.getEntities()) {
			clientEntities.add(entities.getEntity(uid));
		}

		// tell the client everything is ready
		bus.post(new UpdateEvent.Start());
		bus.post(new UpdateEvent.Map(map, clientResources, clientEntities));
	}

	@Subscribe
	private void startOldGame(ServerLoadEvent.Start event) {
		logger.info("loading save character <" + event.save + ">");
	}
	
	/**
	 * Sends a list of all saved characters to the client.
	 * 
	 * @param event
	 */
	@Subscribe
	private void listSavedGames(ServerLoadEvent.List event) {
		String[] saves = FileUtils.listFiles(Paths.get("saves"));
		logger.info("saved characters: " + Arrays.deepToString(saves));
		bus.post(new ClientLoadEvent.List(saves));
	}
	
	/**
	 * Saves the currently running game.
	 * 
	 * @param event
	 * @throws IOException
	 */
	@Subscribe
	private void saveGame(SaveEvent event) throws IOException {
		logger.info("save game");
		// store all cached resources (the maps, basically)
		resources.flush();
		// store all cached entities
		entities.flush();
		// move the temp folder to the saves folder
		FileUtils.moveFolder(Paths.get("temp"), Paths.get("saves", player.toString()));
		// and request JavaFX to quit
		logger.info("quit game");
		Platform.exit();		
	}
	
	/**
	 * Requests the JavaFX runtime to exit the application.
	 * 
	 * @param event
	 */
	@Subscribe
	private void quitGame(QuitEvent event) {
		logger.info("quit game");
		Platform.exit();
	}
}
