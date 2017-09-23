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

import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import neon.common.event.NewGameEvent;
import neon.common.event.ServerEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.RCreature;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityManager;
import neon.entity.entities.Item;
import neon.entity.entities.Player;

/**
 * This class takes care of preparing the engine for a new or saved game.
 * 
 * @author mdriesen
 *
 */
class GameLoader {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	private final ResourceManager resources;
	private final EntityManager entities;
	
	/**
	 * Initializes this game loader.
	 * 
	 * @param bus
	 * @param resources
	 * @param entities
	 */
	GameLoader(EventBus bus, ResourceManager resources, EntityManager entities) {
		this.bus = bus;
		this.resources = resources;
		this.entities = entities;
	}
	
	/**
	 * Prepares all data for a new game and sends this back to the client.
	 * 
	 * @throws ResourceException 
	 */
	void startNewGame(NewGameEvent event) throws ResourceException {
		logger.info("starting a new game");
		
		// get the start map
		CGame game = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", game.getStartMap());
		
		// the player character
		RCreature species = resources.getResource("creatures", event.getSpecies());
		Player player = new Player(event.getName(), event.getGender(), species);
		player.shape.setPosition(game.getStartX(), game.getStartY(), 0);
		entities.addEntity(player);
		
		entities.addEntity(new Item(1, resources.getResource("items", "cup")));
		entities.addEntity(new Item(2, resources.getResource("items", "cup_gold")));
		entities.addEntity(new Item(3, resources.getResource("items", "cup_silver")));
		entities.addEntity(new Item(4, resources.getResource("items", "cup_gold")));
		entities.addEntity(new Item(5, resources.getResource("items", "cup")));
		player.inventory.addItem(1);
		player.inventory.addItem(2);
		player.inventory.addItem(3);
		player.inventory.addItem(4);
		player.inventory.addItem(5);

		// tell the client everything is ready
		bus.post(new UpdateEvent.Start());

		// let the systems know a new game is about to start
		bus.post(new ServerEvent.Start(map));
	}
	
	void startOldGame() {
		logger.info("starting an old game");
	}
}
