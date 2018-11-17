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

import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import neon.common.entity.Action;
import neon.common.entity.Entity;
import neon.common.entity.Skill;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Skills;
import neon.common.entity.components.Stats;
import neon.common.entity.components.Task;
import neon.common.event.CollisionEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.Configuration;
import neon.server.Configuration.GameMode;
import neon.server.entity.EntityManager;
import neon.server.entity.Map;
import neon.server.handlers.SkillHandler;
import neon.util.Direction;

/**
 * The system that handles all movement-related tasks and events.
 * 
 * @author mdriesen
 *
 */
public final class MovementSystem implements NeonSystem {
	private static final Logger logger = Logger.getGlobal();
	
	private final EventBus bus;
	private final EntityManager entities;
	private final ResourceManager resources;
	private final SkillHandler skillHandler;
	private final Configuration config;
	
	MovementSystem(ResourceManager resources, EntityManager entities, EventBus bus, Configuration config) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
		this.config = config;
		skillHandler = new SkillHandler(bus);
	}
	
	/**
	 * Moves the player on the current map.
	 * 
	 * @param player
	 * @param direction
	 * @param map
	 * @throws ResourceException 
	 */
	void move(Entity player, Direction direction, Map map) throws ResourceException {
		Shape shape = player.getComponent(Shape.class);
		int x = shape.getX();
		int y = shape.getY();
		int z = shape.getZ();
		int width = map.getWidth();
		int height = map.getHeight();

		switch (direction) {
		case LEFT: 
			x = Math.max(0, x - 1); 
			break;
		case RIGHT: 
			x = Math.min(width, x + 1); 
			break;
		case UP: 
			y = Math.max(0, y - 1); 
			break;
		case DOWN: 
			y = Math.min(height, y + 1); 
			break;
		case DOWN_LEFT:
			x = Math.max(0, x - 1); 
			y = Math.min(height, y + 1); 
			break;
		case DOWN_RIGHT:
			x = Math.min(width, x + 1); 
			y = Math.min(height, y + 1); 
			break;
		case UP_LEFT:
			x = Math.max(0, x - 1); 
			y = Math.max(0, y - 1); 
			break;
		case UP_RIGHT:
			x = Math.min(width, x + 1); 
			y = Math.max(0, y - 1); 
			break;
		default:
			break;
		}
		
		// check for collisions with other creatures
		for (long uid : map.getEntities(x, y)) {
			if (entities.getEntity(uid).hasComponent(CreatureInfo.class)) {
				// pause the server while the collision is handled
				config.setMode(GameMode.TURN_BASED);
				bus.post(new CollisionEvent(player.uid, uid));
				return;
			}
		}
		
		// move the player
		move(player, map, x, y, z);
	}

	@Override
	public Optional<Entity> update(Entity creature) {
		if (creature.hasComponent(Task.Move.class)) {
			// move the creature if it has a move task
			Task.Move task = creature.getComponent(Task.Move.class);
			Shape shape = creature.getComponent(Shape.class);
			move(creature, task.getMap(), task.x, task.y, shape.getZ());
			task.getMap().moveEntity(creature.uid, task.x, task.y);
			creature.removeComponent(Task.Move.class);
			if (creature.getComponent(Stats.class).isActive()) {
				// if the creature has more action points, let it think again
				creature.setComponent(new Task.Think(creature.uid));
				return Optional.of(creature);
			} else {
				// if not, the creature is out
				return Optional.empty();
			}
		} else {
			// if not, reschedule the creature
			return Optional.of(creature);
		}
	}

	private void move(Entity creature, Map map, int x, int y, int z) {
		Shape shape = creature.getComponent(Shape.class);
		Stats stats = creature.getComponent(Stats.class);
		Skills skills = creature.getComponent(Skills.class);
		
		if (shape.getX() == x || shape.getY() == y) {
			stats.perform(Action.MOVE_STRAIGHT);
		} else {
			stats.perform(Action.MOVE_DIAGONAL);
		}

		boolean canMove = true;

		try {
			RTerrain terrain = resources.getResource("terrain", map.getTerrain().get(x, y));
			if (terrain.hasModifier(RTerrain.Modifier.LIQUID)) {
				canMove = skillHandler.checkSkill(skills, Skill.SWIMMING, stats);
			}
		} catch (ResourceException e) {
			logger.severe("unknown terrain type: " + map.getTerrain().get(x, y));
		}

		if (canMove) {				
			shape.setPosition(x, y, z);				
			bus.post(new UpdateEvent.Move(creature.uid, map.getUid(), shape.getX(), shape.getY(), shape.getZ()));
		}
	}
}
