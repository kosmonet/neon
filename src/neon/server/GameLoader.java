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

import neon.system.event.UpdateEvent;
import neon.system.resources.CGame;
import neon.system.resources.RMap;
import neon.system.resources.Resource;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

class GameLoader {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	private final ResourceManager resources;
	
	GameLoader(EventBus bus, ResourceManager resources) {
		this.bus = bus;
		this.resources = resources;
	}
	
	/**
	 * Prepares all data for a new game and sends this back to the client.
	 * 
	 * @throws ResourceException 
	 */
	void startNewGame() throws ResourceException {
		logger.info("starting a new game");
		
		// get the start map
		CGame game = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", game.getStartMap());
		
		// collect all necessary resources to start the game
		Set<Resource> set = new HashSet<>();
		set.add(game);
		set.add(map);
		
		// add all terrain resources
		for (String terrain : map.getTerrain().getLeaves().values()) {
			set.add(resources.getResource("terrain", terrain));
		}
		
		// and send everything back to the client
		bus.post(new UpdateEvent.Start(set));
	}
	
	void startOldGame() {
		System.out.println("starting game");
	}
}
