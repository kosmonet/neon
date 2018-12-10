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

package neon.systems.ai;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import neon.common.entity.Entity;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
import neon.common.entity.components.Task;
import neon.server.Configuration;
import neon.server.entity.Map;
import neon.server.systems.NeonSystem;

public final class AISystem implements NeonSystem {
	private final Random random = new Random();
	private final Configuration config;
	
	public AISystem(Configuration config) {
		this.config = Objects.requireNonNull(config, "configuration");
	}
	
	private void act(Entity creature) {
		Map map = config.getCurrentMap();
		Shape shape = creature.getComponent(Shape.class);

		// move the creature
		int x = shape.getX() + random.nextInt(3) - 1;
		int y = shape.getY() + random.nextInt(3) - 1;
		creature.setComponent(new Task.Move(creature.uid, x, y, map));
	}

	@Override
	public Optional<Entity> update(Entity creature) {
		if (creature.hasComponent(Task.Think.class) && creature.hasComponent(Behavior.class)) {
			// let the creature schedule an action if it has a thinking task
			Stats stats = creature.getComponent(Stats.class);
			if (stats.isActive()) {
				act(creature);
			}
			creature.removeComponent(Task.Think.class);
			return Optional.of(creature);
		} else {
			// reschedule the creature otherwise
			return Optional.of(creature);
		}
	}
}
