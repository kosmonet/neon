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

package neon.server.systems;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Objects;
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
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.Configuration;
import neon.server.Configuration.GameMode;
import neon.server.entity.EntityManager;
import neon.systems.ai.AISystem;
import neon.systems.combat.CombatSystem;

/**
 * Handles all game systems (the game loop, basically).
 * 
 * @author mdriesen
 * 
 */
public final class SystemManager {
	private final EntityManager entities;
	private final AISystem aiSystem;
	private final ActionSystem actionSystem;
	private final MovementSystem moveSystem;
	private final InputSystem inputSystem;
	private final CombatSystem combatSystem;
	private final Configuration config;
	
	/**
	 * Initializes the system manager. The entity manager and configuration 
	 * must not be null.
	 * 
	 * @param resources
	 * @param entities
	 * @param bus
	 * @param config
	 */
	public SystemManager(ResourceManager resources, EntityManager entities, EventBus bus, Configuration config) {
		this.entities = Objects.requireNonNull(entities, "entity manager");
		this.config = Objects.requireNonNull(config, "configuration");
		
		// create all systems
		moveSystem = new MovementSystem(resources, entities, bus, config);
		aiSystem = new AISystem(config);
		actionSystem = new ActionSystem(bus);
		inputSystem = new InputSystem(entities, bus, moveSystem, config);
		combatSystem = new CombatSystem(entities, bus);
		
		// and register them on the event bus
		bus.register(combatSystem);
		bus.register(inputSystem);
		bus.register(aiSystem);
	}
	
	/**
	 * Handles a change of maps.
	 * 
	 * @param event	an event that describes the map change
	 * @throws ResourceException
	 * @throws IOException
	 */
	@Subscribe
	private void onMapChange(UpdateEvent.Map event) throws ResourceException, IOException {
		config.setCurrentMap(entities.getMap(event.id));
	}
	
	/**
	 * Handles the start of a game.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) {
		config.getCalendar().addTicks(event.time);
		config.setRunning(true);
	}
	
	/**
	 * Handles pausing of a game.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onPause(InputEvent.Pause event) {
		config.setMode(GameMode.TURN_BASED);
	}
	
	/**
	 * Handles unpausing of a game.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onUnpause(InputEvent.Unpause event) {
		config.setMode(GameMode.REAL_TIME);
	}
	
	/**
	 * Handles the game loop timer ticks.
	 * 
	 * @param event	an event that describes the timer tick
	 */
	@Subscribe
	private void onTimerTick(TimerEvent event) {
		if (config.isRunning() && config.getMode().equals(GameMode.REAL_TIME)) {
			update(Configuration.TICKS_PER_TURN);
		}
	}

	/**
	 * Handles the next turn.
	 * 
	 * @param event	an event that describes the next turn
	 */
	@Subscribe
	private void onNextTurn(TurnEvent event) {
		if (config.isRunning() && config.getMode().equals(GameMode.TURN_BASED)) {
			update(1);
		}
	}

	/**
	 * Returns all entities that are currently active.
	 * 
	 * @return	a {@code Collection<Long>} of entity uid's
	 */
	private Collection<Long> getActiveEntities() {
		return config.getCurrentMap().getEntities();
	}
	
	/**
	 * Advances the game state for a fraction of a turn.
	 * 
	 * @param fraction	the fraction of a turn
	 */
	private void update(int fraction) {
		// update the player separately for now
		Entity player = entities.getEntity(Configuration.PLAYER_UID);
		player.setComponent(new Task.Action(Configuration.PLAYER_UID, fraction));
		actionSystem.update(player);
		
		// collect all active creatures on the current map and mark them for updates
		Deque<Entity> creatures = getActiveEntities().stream()
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
