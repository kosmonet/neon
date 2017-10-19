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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.MoveEvent;
import neon.common.event.ThinkEvent;
import neon.entity.entities.Creature;

public class AISystem {
	private final Random random = new Random();
	private final EventBus bus;
	
	public AISystem(EventBus bus) {
		this.bus = bus;
	}
	
	@Subscribe
	private void act(ThinkEvent event) {
		Creature creature = event.creature;
		
		// move the creature
		int x = creature.shape.getX() + random.nextInt(3) - 1;
		int y = creature.shape.getY() + random.nextInt(3) - 1;
		bus.post(new MoveEvent.Start(creature, new Point(x, y)));		
	}
}
