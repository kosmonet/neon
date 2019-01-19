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

import java.util.Objects;

import com.google.common.eventbus.Subscribe;

import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.Map;
import neon.common.entity.components.Component;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentEvent;
import neon.common.event.UpdateEvent;

/**
 * A class to handle all entity-related events.
 * 
 * @author mdriesen
 *
 */
public class EntityHandler {
	private final ComponentManager components;
	private final Configuration config;
	
	/**
	 * The component manager and configuration must not be null.
	 * 
	 * @param components	the client component manager
	 * @param config	the client configuration data
	 */
	public EntityHandler(ComponentManager components, Configuration config) {
		this.components = Objects.requireNonNull(components, "component manager");
		this.config = Objects.requireNonNull(config, "configuration");
	}
	
	/**
	 * Handles a component update.
	 * 
	 * @param event	a {@code ComponentEvent} describing the update
	 * @throws ClassNotFoundException	if the event doesn't contain a valid component
	 */
	@Subscribe 
	private void onComponentUpdate(ComponentEvent event) throws ClassNotFoundException {
		Component component = event.getComponent();
		components.putComponent(component);
	}
	
	/**
	 * Moves an entity on a map.
	 * 
	 * @param event	an {@code UpdateEvent} describing the movement
	 */
	@Subscribe
	private void onEntityMove(UpdateEvent.Move event) {
		if (components.hasComponent(event.uid, Shape.class)) {
			Shape shape = components.getComponent(event.uid, Shape.class);
			shape.setPosition(event.x, event.y, event.z);			
		} else {
			Shape shape = new Shape(event.uid, event.x, event.y, event.z);
			components.putComponent(shape);
		}

		Map map = config.getCurrentMap();		
		if (map.getEntities().contains(event.uid)) {
			map.moveEntity(event.uid, event.x, event.y);
		} else {
			map.addEntity(event.uid, event.x, event.y);
		}
	}
	
	/**
	 * Removes an entity from a map.
	 * 
	 * @param event	an {@code UpdateEvent} describing the removal
	 */
	@Subscribe
	private void onEntityRemove(UpdateEvent.Remove event) {
		Map map = config.getCurrentMap();
		map.removeEntity(event.uid);
	}
	
	/**
	 * Completely removes an entity from the game.
	 * 
	 * @param event	an {@code UpdateEvent} describing the removal
	 */
	@Subscribe
	private void onEntityDestroy(UpdateEvent.Destroy event) {
		components.removeEntity(event.uid);
	}
}
