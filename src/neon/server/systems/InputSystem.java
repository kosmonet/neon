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

import neon.common.entity.Entity;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
import neon.common.event.InputEvent;
import neon.common.event.TurnEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.ResourceException;
import neon.server.Configuration;
import neon.server.entity.EntityManager;
import neon.server.entity.Map;

public final class InputSystem implements NeonSystem {
	private final EntityManager entities;
	private final EventBus bus;
	private final MovementSystem mover;
	private final Configuration config;
	
	public InputSystem(EntityManager entities, EventBus bus, MovementSystem mover, Configuration config) {
		this.bus = bus;
		this.entities = entities;
		this.mover = mover;
		this.config = config;
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 * @throws ResourceException 
	 */
	@Subscribe
	private void move(InputEvent.Move event) throws ResourceException {
		Entity player = entities.getEntity(0);
		Stats stats = player.getComponent(Stats.class);
		
		if(stats.isActive()) {
			Map map = config.getCurrentMap();
			mover.move(player, event.direction, map);

			// signal the client that an entity was updated
			Shape shape = player.getComponent(Shape.class);
			bus.post(new UpdateEvent.Move(0, map.getUid(), shape.getX(), shape.getY(), shape.getZ()));
		}

		// check if the player still has action points left after moving
		if(!stats.isActive()) {
			// if not, go to the next turn
			bus.post(new TurnEvent());
		}
	}
}
