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

import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import neon.common.event.CollisionEvent;
import neon.common.event.TimerEvent;
import neon.common.resources.RMap;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.Action;
import neon.entity.EntityProvider;
import neon.entity.components.Shape;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;
import neon.entity.entities.Player;
import neon.util.Direction;

/**
 * The system that handles all movement-related events.
 * 
 * @author mdriesen
 *
 */
public class MovementSystem implements NeonSystem {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	private final EntityProvider entities;
	private final ResourceManager resources;
	
	public MovementSystem(ResourceManager resources, EntityProvider entities, EventBus bus) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 */
	public void move(Player player, Direction direction, RMap map) {
		Shape shape = player.getComponent(Shape.class);
		int x = shape.getX();
		int y = shape.getY();
		int z = shape.getZ();

		switch (direction) {
		case LEFT: x = Math.max(0, x - 1); break;
		case RIGHT: x = Math.min(map.getWidth(), x + 1); break;
		case UP: y = Math.max(0, y - 1); break;
		case DOWN: y = Math.min(map.getHeight(), y + 1); break;
		}
		
		// check for collisions with other creatures
		if (!map.getEntities(x, y).isEmpty()) {
			for (long uid : map.getEntities(x, y)) {
				if (entities.getEntity(uid) instanceof Creature) {
					bus.post(new CollisionEvent(player.uid, uid));
					return;
				}
			}
		}
		
		// move the player
		try {
			move(player, map, x, y, z);
		} catch (ResourceException e) {
			logger.severe("unknown terrain type: " + map.getTerrain().get(x, y));
		}
	}

	/**
	 * Moves a creature on the current map.
	 * 
	 * @param event
	 */
	public void move(Creature creature, int x, int y, RMap map) {
		try {
			Shape shape = creature.getComponent(Shape.class);
			move(creature, map, x, y, shape.getZ());
			map.moveEntity(creature.uid, x, y);
		} catch (ResourceException e) {
			logger.severe("unknown terrain type: " + map.getTerrain().get(x, y));
		}
	}
	
	private void move(Creature creature, RMap map, int x, int y, int z) throws ResourceException {
		Shape shape = creature.getComponent(Shape.class);
		Stats stats = creature.getComponent(Stats.class);
		
		if (shape.getX() == x || shape.getY() == y) {
			stats.perform(Action.MOVE_STRAIGHT);
		} else {
			stats.perform(Action.MOVE_DIAGONAL);
		}

		RTerrain terrain = resources.getResource("terrain", map.getTerrain().get(x, y));
		if (!terrain.hasModifier(RTerrain.Modifier.LIQUID)) {				
			shape.setPosition(x, y, z);				
		}
	}

	@Override
	public void onTimerTick(TimerEvent tick) {}
}
