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

import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.TurnEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.CGame;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Player;
import neon.server.EntityTracker;

public class TurnSystem implements NeonSystem {
	private final ResourceManager resources;
	private final EntityTracker entities;
	private final EventBus bus;
	private final Random random = new Random();
	
	public TurnSystem(ResourceManager resources, EntityTracker entities, EventBus bus) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
	}
	
	@Subscribe
	private void handleTurn(TurnEvent event) throws ResourceException {
		CGame config = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", config.getCurrentMap());
		
		Player player = entities.getEntity(0);
		
		Rectangle bounds = new Rectangle(player.shape.getX() - 50, player.shape.getY() - 50, 100, 100);
		Collection<Entity> changed = new HashSet<Entity>();
		
		for (long uid : map.getEntities(bounds)) {
			Entity entity = entities.getEntity(uid);
			changed.add(entity);
			
			if(entity instanceof Creature) {
				Creature creature = (Creature) entity;
				int x = creature.shape.getX() + random.nextInt(3) - 1;
				int y = creature.shape.getY() + random.nextInt(3) - 1;
				creature.shape.setX(x);
				creature.shape.setY(y);
				map.moveEntity(uid, x, y);
			}
		}
		
		bus.post(new UpdateEvent.Entities(changed));
	}
}
