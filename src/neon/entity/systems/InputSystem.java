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

package neon.entity.systems;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.InputEvent;
import neon.entity.EntityProvider;
import neon.entity.MovementService;
import neon.entity.entities.Player;
import neon.entity.events.TurnEvent;
import neon.entity.events.UpdateEvent;

public class InputSystem implements NeonSystem {
	private final EntityProvider entities;
	private final EventBus bus;
	private final MovementService mover;
	
	public InputSystem(EntityProvider entities, EventBus bus, MovementService mover) {
		this.bus = bus;
		this.entities = entities;
		this.mover = mover;
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 */
	@Subscribe
	private void move(InputEvent.Move event) {
		Player player = (Player) entities.getEntity(0);
		
		if(player.stats.isActive()) {
			mover.move(player, event.getDirection());

			// signal the client that an entity was updated
			bus.post(new UpdateEvent.Entities(player));
		}

		// check if the player still has action points left after moving
		if(!player.stats.isActive()) {
			// if not, go to the next turn
			bus.post(new TurnEvent());
		}
	}
}
