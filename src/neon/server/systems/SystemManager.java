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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Task;
import neon.common.event.InputEvent;
import neon.common.event.TimerEvent;
import neon.common.event.TurnEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.RMap;
import neon.common.resources.CGame.GameMode;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;
import neon.systems.ai.AISystem;
import neon.systems.combat.CombatSystem;
import neon.systems.magic.MagicSystem;

/**
 * Handles all game systems (the game loop, basically).
 * 
 * @author mdriesen
 * 
 */
public final class SystemManager {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	private final ResourceManager resources;
	private final EntityManager entities;
	private final AISystem aiSystem;
	private final ActionSystem actionSystem;
	private final MovementSystem moveSystem;
	private final InputSystem inputSystem;
	private final CombatSystem combatSystem;
	private final MagicSystem magicSystem;
	
	private boolean running = false;
	private CGame config;
	
	public SystemManager(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.resources = resources;
		this.entities = entities;
		
		// create all systems
		moveSystem = new MovementSystem(resources, entities, bus);
		aiSystem = new AISystem(resources);
		actionSystem = new ActionSystem(bus);
		inputSystem = new InputSystem(resources, entities, bus, moveSystem);
		combatSystem = new CombatSystem(entities, bus);
		magicSystem = new MagicSystem(resources, entities, bus);
		
		// and register them on the event bus
		bus.register(combatSystem);
		bus.register(inputSystem);
		bus.register(magicSystem);
		bus.register(aiSystem);
	}
	
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) throws ResourceException {
		config = resources.getResource("config", "game");
		running = true;
	}
	
	@Subscribe
	private void onPause(InputEvent.Pause event) {
		config.setMode(GameMode.TURN_BASED);
	}
	
	@Subscribe
	private void onUnpause(InputEvent.Unpause event) {
		config.setMode(GameMode.REAL_TIME);
	}
	
	@Subscribe
	private void onTimerTick(TimerEvent event) throws ResourceException {
		if (running && config.getMode().equals(GameMode.REAL_TIME)) {
			update(5);
		}
	}

	@Subscribe
	private void onNextTurn(TurnEvent event) throws ResourceException {
		if (running && config.getMode().equals(GameMode.TURN_BASED)) {
			update(1);
		}
	}

	private Collection<Long> getActiveEntities() {
		HashSet<Long> entities = new HashSet<>();
		try {
			RMap map = resources.getResource("maps", config.getCurrentMap());
			entities.addAll(map.getEntities());
		} catch (ResourceException e) {
			logger.severe("unknown map id: <" + config.getCurrentMap() + ">");
		}
		return entities;
	}
	
	/**
	 * Updates the game state.
	 */
	private void update(int fraction) {
		// update the player separately for now
		Entity player = entities.getEntity(PLAYER_UID);
		player.setComponent(new Task.Action(PLAYER_UID, fraction));
		actionSystem.update(player);
		
		// collect all active creatures on the current map and mark them for updates
		ArrayDeque<Entity> creatures = getActiveEntities().parallelStream()
				.map(entities::getEntity).filter(entity -> entity.hasComponent(CreatureInfo.class))
				.peek(entity -> entity.setComponent(new Task.Action(entity.uid, fraction)))
				.collect(Collectors.toCollection(ArrayDeque::new));

		// iterate over the creatures until none is left with open tasks
		while (!creatures.isEmpty()) {
			try {
				actionSystem.update(creatures.pop()).ifPresent(creatures::push);
				aiSystem.update(creatures.pop()).ifPresent(creatures::push);
				moveSystem.update(creatures.pop()).ifPresent(creatures::push);
			} catch (NoSuchElementException e) {
				continue;	// in case the stack runs empty while iterating
			}
		}
	}
}
