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

import java.awt.Point;

import com.google.common.eventbus.EventBus;

import neon.common.event.CollisionEvent;
import neon.common.resources.RMap;
import neon.entity.Action;
import neon.entity.EntityProvider;
import neon.entity.entities.Creature;
import neon.entity.entities.Player;
import neon.util.Direction;

/**
 * The system that handles all movement-related events.
 * 
 * @author mdriesen
 *
 */
public class MovementSystem {
	private final EventBus bus;
	private final EntityProvider entities;
	
	public MovementSystem(EntityProvider entities, EventBus bus) {
		this.entities = entities;
		this.bus = bus;
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 */
	public void move(Player player, Direction direction, RMap map) {
		int x = player.shape.getX();
		int y = player.shape.getY();
		int z = player.shape.getZ();
		
		switch (direction) {
		case LEFT: x = Math.max(0, x - 1); break;
		case RIGHT: x = Math.min(map.getWidth(), x + 1); break;
		case UP: y = Math.max(0, y - 1); break;
		case DOWN: y = Math.min(map.getHeight(), y + 1); break;
		}
		
		if (!map.getEntities(x, y).isEmpty()) {
			for (long uid : map.getEntities(x, y)) {
				if (entities.getEntity(uid) instanceof Creature) {
					bus.post(new CollisionEvent(player.uid, uid));
				}
			}
		} else {
			player.shape.setPosition(x, y, z);
			player.stats.perform(Action.MOVE_STRAIGHT);			
		}
	}

	/**
	 * Moves a creature on the current map.
	 * 
	 * @param event
	 */
	public void move(Creature creature, Point position, RMap map) {
		if (creature.shape.getX() == position.x || creature.shape.getY() == position.y) {
			creature.stats.perform(Action.MOVE_STRAIGHT);			
		} else {
			creature.stats.perform(Action.MOVE_DIAGONAL);				
		}
		creature.shape.setX(position.x);
		creature.shape.setY(position.y);
		map.moveEntity(creature.uid, position.x, position.y);
	}
}
