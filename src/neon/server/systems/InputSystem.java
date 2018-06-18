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

package neon.server.systems;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.InputEvent;
import neon.common.event.TurnEvent;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.components.ShapeComponent;
import neon.entity.components.StatsComponent;
import neon.entity.entities.Player;
import neon.entity.events.UpdateEvent;

public class InputSystem implements NeonSystem {
	private final EntityProvider entities;
	private final EventBus bus;
	private final MovementSystem mover;
	private final ResourceManager resources;
	
	public InputSystem(ResourceManager resources, EntityProvider entities, EventBus bus, MovementSystem mover) {
		this.bus = bus;
		this.entities = entities;
		this.mover = mover;
		this.resources = resources;
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 * @throws ResourceException 
	 */
	@Subscribe
	private void move(InputEvent.Move event) throws ResourceException {
		Player player = (Player) entities.getEntity(0);
		StatsComponent stats = player.getComponent("stats");
		
		if(stats.isActive()) {
			RMap map = resources.getResource("maps", event.getMap());
			mover.move(player, event.getDirection(), map);

			// signal the client that an entity was updated
			ShapeComponent shape = player.getComponent("shape");
			bus.post(new UpdateEvent.Move(0, shape.getX(), shape.getY(), shape.getZ()));
		}

		// check if the player still has action points left after moving
		if(!stats.isActive()) {
			// if not, go to the next turn
			bus.post(new TurnEvent());
		}
	}
}
