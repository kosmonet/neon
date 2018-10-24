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

import java.util.Random;

import com.google.common.eventbus.EventBus;

import neon.common.entity.components.Behavior;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
import neon.common.entity.entities.Entity;
import neon.common.event.TimerEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

public class AISystem implements NeonSystem {
	private final Random random = new Random();
	private final MovementSystem mover;
	private final EntityManager entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	AISystem(ResourceManager resources, EntityManager entities, EventBus bus, MovementSystem mover) {
		this.mover = mover;
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
	}
	
	private void act(Entity creature, RMap map) {
		Stats stats = creature.getComponent(Stats.class);
		
		while (stats.isActive()) {
			// move the creature
			Shape shape = creature.getComponent(Shape.class);
			int x = shape.getX() + random.nextInt(3) - 1;
			int y = shape.getY() + random.nextInt(3) - 1;
			mover.move(creature, x, y, map);
		}
	}

	void run() throws ResourceException {
		CGame config = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", config.getCurrentMap());
				
		// get all entities in the player's neighbourhood
		for (long uid : map.getEntities()) {
			Entity entity = entities.getEntity(uid);
			if (entity.hasComponent(Behavior.class)) {
				Stats stats = entity.getComponent(Stats.class);

				// let the creature act
				if (stats.isActive()) {
					act(entity, map);
				}
				
				// let the client know that an entity has moved
				Shape shape = entity.getComponent(Shape.class);
				bus.post(new UpdateEvent.Move(uid, map.id, shape.getX(), shape.getY(), shape.getZ()));
			}
		}		
	}
	
	@Override
	public void onTimerTick(TimerEvent tick) {
		
	}
}
