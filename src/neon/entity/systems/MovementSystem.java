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

import java.util.HashSet;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.InputEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.RMap;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityManager;
import neon.entity.SystemEvent;
import neon.entity.entities.Entity;

/**
 * The system that handles all movement-related events.
 * 
 * @author mdriesen
 *
 */
public class MovementSystem {
	private final EntityManager entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public MovementSystem(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.entities = entities;
		this.resources = resources;
		this.bus = bus;
	}
	
	@Subscribe
	private void start(SystemEvent.Start event) throws ResourceException {
		// add the player to the start map
		RMap map = event.getMap();
		map.getEntities().add(0l);
		
		// collect all necessary resources to start the game
		Set<Resource> clientResources = new HashSet<>();
		clientResources.add(resources.getResource("config", "game"));
		clientResources.add(map);
		
		// add all terrain resources
		for (String terrain : map.getTerrain().getLeaves().values()) {
			clientResources.add(resources.getResource("terrain", terrain));
		}
		
		// collect all the necessary entities
		Set<Entity> clientEntities = new HashSet<>();
		clientEntities.add(entities.getEntity(0));

		// tell the client everything is ready
		bus.post(new UpdateEvent.Map(map, clientResources, clientEntities));
	}
	
	@Subscribe
	private void move(InputEvent.Move event) {
		System.out.println(event.getDirection());
	}
}
