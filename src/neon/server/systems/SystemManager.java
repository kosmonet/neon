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

package neon.server.systems;

import java.awt.Rectangle;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.NeonEvent;
import neon.common.event.TimerEvent;
import neon.common.event.TurnEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.GameMode;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.components.Shape;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;

/**
 * Handles all game systems (the game loop, basically).
 * 
 * @author mdriesen
 * 
 */
public class SystemManager {
	private final ResourceManager resources;
	private final EntityProvider entities;
	private final EventBus bus;
	
	private final AISystem aiSystem;
	private final ActionSystem actionSystem;
	private final MovementSystem moveSystem;
	private final InputSystem inputSystem;
	private final CombatSystem combatSystem;
	
	private boolean running = false;
	
	public SystemManager(ResourceManager resources, EntityProvider entities, EventBus bus) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
		
		// create all systems
		moveSystem = new MovementSystem(resources, entities, bus);
		aiSystem = new AISystem(moveSystem);
		actionSystem = new ActionSystem(resources, entities);
		inputSystem = new InputSystem(resources, entities, bus, moveSystem);
		combatSystem = new CombatSystem(entities);
		
		// and register them on the event bus
		bus.register(combatSystem);
		bus.register(inputSystem);

	}
	
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) {
		running = true;
	}
	
	@Subscribe
	private void setMode(NeonEvent.Pause event) throws ResourceException {
		CGame config = resources.getResource("config", "game");
		config.setMode(GameMode.TURN_BASED);
	}
	
	@Subscribe
	private void setMode(NeonEvent.Unpause event) throws ResourceException {
		CGame config = resources.getResource("config", "game");
		config.setMode(GameMode.REAL_TIME);
	}
	
	@Subscribe
	private void onTimerTick(TimerEvent event) throws ResourceException {
		if (running) {
			CGame config = resources.getResource("config", "game");
			if (config.getMode().equals(GameMode.REAL_TIME)) {
				update();
			}
		}
	}

	@Subscribe
	private void onNextTurn(TurnEvent event) throws ResourceException {
		if (running) {
			CGame config = resources.getResource("config", "game");
			if (config.getMode().equals(GameMode.TURN_BASED)) {
				update();
			}
		}
	}

	/**
	 * Updates the game for the given fraction of a full turn.
	 * 
	 * @param fraction
	 * @throws ResourceException 
	 */
	private void update() throws ResourceException {
		actionSystem.run();
		
		CGame config = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", config.getCurrentMap());
				
		// get all entities in the player's neighbourhood
		Shape center = entities.getEntity(0).getComponent(Shape.class);
		Rectangle bounds = new Rectangle(center.getX() - 50, center.getY() - 50, 100, 100);
		for (long uid : map.getEntities(bounds)) {
			Entity entity = entities.getEntity(uid);
			if (entity instanceof Creature) {
				Creature creature = (Creature) entity;
				Stats creatureStats = creature.getComponent(Stats.class);

				// let the creature act
				if(creatureStats.isActive()) {
					aiSystem.act(creature, map);
				}
				
				// let the client know that an entity has moved
				Shape shape = creature.getComponent(Shape.class);
				bus.post(new UpdateEvent.Move(uid, map.id, shape.getX(), shape.getY(), shape.getZ()));
			}
		}
	}
}
