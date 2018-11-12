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

package neon.client.handlers;

import com.google.common.eventbus.Subscribe;

import neon.client.ComponentManager;
import neon.common.entity.components.Component;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

/**
 * A class to handle all entity-related events.
 * 
 * @author mdriesen
 *
 */
public class EntityHandler {
	private final ComponentManager components;
	private final ResourceManager resources;
	
	public EntityHandler(ComponentManager components, ResourceManager resources) {
		this.components = components;
		this.resources = resources;
	}
	
	@Subscribe 
	private void onComponentUpdate(ComponentUpdateEvent event) throws ClassNotFoundException {
		Component component = event.getComponent();
		components.putComponent(component);
	}
	
	@Subscribe
	private void onEntityMove(UpdateEvent.Move event) throws ResourceException {
		if (components.hasComponent(event.uid, Shape.class)) {
			Shape shape = components.getComponent(event.uid, Shape.class);
			shape.setPosition(event.x, event.y, event.z);			
		} else {
			Shape shape = new Shape(event.uid, event.x, event.y, event.z);
			components.putComponent(shape);
		}

		RMap map = resources.getResource("maps", event.map);		
		if (map.getEntities().contains(event.uid)) {
			map.moveEntity(event.uid, event.x, event.y);
		} else {
			map.addEntity(event.uid, event.x, event.y);
		}
	}
	
	@Subscribe
	private void onEntityRemove(UpdateEvent.Remove event) throws ResourceException {
		RMap map = resources.getResource("maps", event.map);
		map.removeEntity(event.uid);
	}
	
	@Subscribe
	private void onEntityDestroy(UpdateEvent.Destroy event) throws ResourceException {
		components.removeEntity(event.uid);
	}
}
