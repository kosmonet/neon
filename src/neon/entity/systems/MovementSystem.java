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
import neon.common.event.UpdateEvent;
import neon.common.resources.ResourceManager;
import neon.entity.entities.Player;
import neon.server.EntityTracker;

/**
 * The system that handles all movement-related events.
 * 
 * @author mdriesen
 *
 */
public class MovementSystem implements NeonSystem {
	private final EntityTracker entities;
	private final EventBus bus;
	
	public MovementSystem(ResourceManager resources, EntityTracker entities, EventBus bus) {
		this.entities = entities;
		this.bus = bus;
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 */
	@Subscribe
	private void move(InputEvent.Move event) {
		Player player = (Player) entities.getEntity(0);
		
		switch(event.getDirection()) {
		case "left": player.shape.setX(Math.max(0, player.shape.getX() - 1)); break;
		case "right": player.shape.setX(player.shape.getX() + 1); break;
		case "up": player.shape.setY(Math.max(0, player.shape.getY() - 1)); break;
		case "down": player.shape.setY(player.shape.getY() + 1); break;
		}
		
		bus.post(new UpdateEvent.Entities(player));
	}
}
