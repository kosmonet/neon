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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.NeonEvent;
import neon.common.event.TimerEvent;
import neon.common.event.TurnEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.GameMode;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;

/**
 * Handles all game systems (the game loop, basically).
 * 
 * @author mdriesen
 * 
 */
public class SystemManager {
	private final ResourceManager resources;
	
	private final AISystem aiSystem;
	private final ActionSystem actionSystem;
	private final MovementSystem moveSystem;
	private final InputSystem inputSystem;
	private final CombatSystem combatSystem;
	
	private boolean running = false;
	private CGame config;
	
	public SystemManager(ResourceManager resources, EntityProvider entities, EventBus bus) {
		this.resources = resources;
		
		// create all systems
		moveSystem = new MovementSystem(resources, entities, bus);
		aiSystem = new AISystem(resources, entities, bus, moveSystem);
		actionSystem = new ActionSystem(resources, entities);
		inputSystem = new InputSystem(resources, entities, bus, moveSystem);
		combatSystem = new CombatSystem(entities, bus);
		
		// and register them on the event bus
		bus.register(combatSystem);
		bus.register(inputSystem);

	}
	
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) throws ResourceException {
		config = resources.getResource("config", "game");
		running = true;
	}
	
	@Subscribe
	private void onPause(NeonEvent.Pause event) throws ResourceException {
		config.setMode(GameMode.TURN_BASED);
	}
	
	@Subscribe
	private void onUnpause(NeonEvent.Unpause event) throws ResourceException {
		config.setMode(GameMode.REAL_TIME);
	}
	
	@Subscribe
	private void onTimerTick(TimerEvent event) throws ResourceException {
		if (running) {
			if (config.getMode().equals(GameMode.REAL_TIME)) {
				update();
			}
		}
	}

	@Subscribe
	private void onNextTurn(TurnEvent event) throws ResourceException {
		if (running) {
			if (config.getMode().equals(GameMode.TURN_BASED)) {
				update();
			}
		}
	}

	/**
	 * Updates the game state.
	 *
	 * @throws ResourceException 
	 */
	private void update() throws ResourceException {
		actionSystem.run();
		aiSystem.run();
//		moveSystem.run();
	}
}
