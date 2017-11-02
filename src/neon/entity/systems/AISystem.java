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

import java.awt.Point;
import java.util.Random;

import com.google.common.eventbus.Subscribe;

import neon.entity.MovementService;
import neon.entity.entities.Creature;
import neon.entity.events.ThinkEvent;

public class AISystem implements NeonSystem {
	private final Random random = new Random();
	private final MovementService mover;
	
	public AISystem(MovementService mover) {
		this.mover = mover;
	}
	
	@Subscribe
	private void act(ThinkEvent event) {
		Creature creature = event.creature;
		
		while(creature.stats.isActive()) {
			// move the creature
			int x = creature.shape.getX() + random.nextInt(3) - 1;
			int y = creature.shape.getY() + random.nextInt(3) - 1;
			mover.move(creature, new Point(x, y), event.map);
		}
	}
}
