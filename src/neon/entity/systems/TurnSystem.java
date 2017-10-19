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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.ThinkEvent;
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

/**
 * System to handle new turns.
 * 
 * @author mdriesen
 * 
 */
public class TurnSystem implements NeonSystem {
	private final ResourceManager resources;
	private final EntityTracker entities;
	private final EventBus bus;
	
	public TurnSystem(ResourceManager resources, EntityTracker entities, EventBus bus) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
	}
	
	@Subscribe
	private void handleTurn(TurnEvent event) throws ResourceException {
		CGame config = resources.getResource("config", "game");
		RMap map = resources.getResource("maps", config.getCurrentMap());
		
		// reset the player's action points
		Player player = entities.getEntity(0);
		player.stats.rest();
		
		// get all entities in the player's neighbourhood
		Rectangle bounds = new Rectangle(player.shape.getX() - 50, player.shape.getY() - 50, 100, 100);
		Collection<Entity> changed = new HashSet<Entity>();
		
		for (long uid : map.getEntities(bounds)) {
			Entity entity = entities.getEntity(uid);
			changed.add(entity);
			
			if (entity instanceof Creature) {
				// reset the creature's action points
				Creature creature = (Creature) entity;
				creature.stats.rest();

				// let the creature act
				bus.post(new ThinkEvent(creature));
				// beware: these events are handled AFTER handleTurn has finished
			}
		}
		
		// let the client know that entities have changed and should be redrawn
		bus.post(new UpdateEvent.Entities(changed));
	}
}
