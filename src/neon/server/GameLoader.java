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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import neon.entity.EntityManager;
import neon.entity.entities.Entity;
import neon.entity.entities.Player;
import neon.system.event.NewGameEvent;
import neon.system.event.UpdateEvent;
import neon.system.resources.CGame;
import neon.system.resources.RCreature;
import neon.system.resources.RMap;
import neon.system.resources.Resource;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

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
		
		// collect all necessary resources to start the game
		Set<Resource> clientResources = new HashSet<>();
		clientResources.add(game);
		clientResources.add(map);
		
		// add all terrain resources
		for (String terrain : map.getTerrain().getLeaves().values()) {
			clientResources.add(resources.getResource("terrain", terrain));
		}
		
		// collect all the necessary entities
		Set<Entity> clientEntities = new HashSet<>();
		
		// the player character
		RCreature species = resources.getResource("creatures", event.getSpecies());
		Player player = new Player(event.getName(), event.getGender(), species);
		player.shape.setPosition(game.getStartX(), game.getStartY());
		entities.addEntity(player);
		clientEntities.add(player);
		
		// and send everything back to the client
		bus.post(new UpdateEvent.Start(clientResources, clientEntities));
	}
	
	void startOldGame() {
		logger.info("starting an old game");
	}
}
