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

import java.awt.Rectangle;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.NeonEvent;
import neon.common.event.TimerEvent;
import neon.common.event.TurnEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.components.Shape;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Player;
import neon.server.systems.AISystem;

/**
 * Handles new turns (the game loop, basically).
 * 
 * @author mdriesen
 * 
 */
class TurnHandler {
	private final ResourceManager resources;
	private final EntityProvider entities;
	private final EventBus bus;
	private final AISystem ai;
	
	private GameMode mode = GameMode.TURN_BASED;
	
	TurnHandler(ResourceManager resources, EntityProvider entities, EventBus bus, AISystem ai) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
		this.ai = ai;
	}
	
	@Subscribe
	private void setMode(NeonEvent.Pause event) {
		mode = GameMode.TURN_BASED;
	}
	
	@Subscribe
	private void setMode(NeonEvent.Unpause event) {
		mode = GameMode.REAL_TIME;
	}
	
	@Subscribe
	private void handleTick(TimerEvent event) throws ResourceException {
		if (mode == GameMode.REAL_TIME) {
			// update the world
			update(5);
		}
	}

	@Subscribe
	private void handleTurn(TurnEvent event) throws ResourceException {
		if (mode == GameMode.TURN_BASED) {
			// update the world
			update(1);
		}
	}
	
	/**
	 * Updates the game for the given fraction of a full turn.
	 * 
	 * @param fraction
	 * @throws ResourceException 
	 */
	private void update(int fraction) throws ResourceException {
		CGame config = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", config.getCurrentMap());
		
		// restore the player's action points
		Player player = entities.getEntity(0);
		Stats playerStats = player.getComponent(Stats.class);
		playerStats.restoreAP(fraction);
		
		// get all entities in the player's neighbourhood
		Shape center = player.getComponent(Shape.class);
		Rectangle bounds = new Rectangle(center.getX() - 50, center.getY() - 50, 100, 100);
		for (long uid : map.getEntities(bounds)) {
			Entity entity = entities.getEntity(uid);
			if (entity instanceof Creature) {
				Creature creature = (Creature) entity;
				Stats creatureStats = creature.getComponent(Stats.class);

				// reset the creature's action points
				creatureStats.restoreAP(fraction);

				// let the creature act
				if(creatureStats.isActive()) {
					ai.act(creature, map);
				}
				
				// let the client know that an entity has moved
				Shape shape = creature.getComponent(Shape.class);
				bus.post(new UpdateEvent.Move(uid, map.id, shape.getX(), shape.getY(), shape.getZ()));
			}
		}
	}
}
