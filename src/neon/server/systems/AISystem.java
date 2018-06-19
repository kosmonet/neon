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

import java.util.Random;

import neon.common.resources.RMap;
import neon.entity.components.Shape;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;

public class AISystem implements NeonSystem {
	private final Random random = new Random();
	private final MovementSystem mover;
	
	public AISystem(MovementSystem mover) {
		this.mover = mover;
	}
	
	public void act(Creature creature, RMap map) {
		Stats stats = creature.getComponent(Stats.class);
		
		while (stats.isActive()) {
			// move the creature
			Shape shape = creature.getComponent(Shape.class);
			int x = shape.getX() + random.nextInt(3) - 1;
			int y = shape.getY() + random.nextInt(3) - 1;
			mover.move(creature, x, y, map);
		}
	}
}
