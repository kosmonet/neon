/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

import java.awt.Rectangle;

import neon.common.event.TimerEvent;
import neon.common.resources.CGame;
import neon.common.resources.GameMode;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.components.Shape;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Player;

public class ActionSystem implements NeonSystem, Runnable {
	private final EntityProvider entities;
	private final ResourceManager resources;
	
	public ActionSystem(ResourceManager resources, EntityProvider entities) {
		this.entities = entities;
		this.resources = resources;
	}
	
	@Override
	public void run() {
		try {
			CGame config = resources.getResource("config", "game");
			int fraction = config.getMode().equals(GameMode.REAL_TIME) ? 5 : 1;

			// restore the player's action points
			Player player = entities.getEntity(0);
			Stats playerStats = player.getComponent(Stats.class);
			playerStats.restoreAP(fraction);		

			RMap map = resources.getResource("maps", config.getCurrentMap());

			Shape center = entities.getEntity(0).getComponent(Shape.class);
			Rectangle bounds = new Rectangle(center.getX() - 50, center.getY() - 50, 100, 100);
			for (long uid : map.getEntities(bounds)) {
				Entity entity = entities.getEntity(uid);
				if (entity instanceof Creature) {
					Creature creature = (Creature) entity;
					Stats creatureStats = creature.getComponent(Stats.class);

					// reset the creature's action points
					creatureStats.restoreAP(1);
				}
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTimerTick(TimerEvent tick) {
		
	}
}
