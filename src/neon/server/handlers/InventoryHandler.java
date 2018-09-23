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

package neon.server.handlers;

import java.util.ArrayList;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.InventoryEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.components.Inventory;
import neon.entity.components.Shape;
import neon.entity.entities.Item;
import neon.entity.entities.Player;

/**
 * This class handles the inventory-related server bits.
 * 
 * @author mdriesen
 *
 */
public class InventoryHandler {
	private final EventBus bus;
	private final EntityProvider entities;
	private final ResourceManager resources;
	
	public InventoryHandler(ResourceManager resources, EntityProvider entities, EventBus bus) {
		this.bus = bus;
		this.entities = entities;
		this.resources = resources;
	}
	
	@Subscribe
	private void postInventory(InventoryEvent.Request event) {
		Player player = entities.getEntity(0);
		ArrayList<Long> items = new ArrayList<>();
		Inventory inventory = player.getComponent(Inventory.class);
		items.addAll(inventory.getItems());
		bus.post(new InventoryEvent.List(items, inventory.getMoney()));
	}
	
	@Subscribe
	private void drop(InventoryEvent.Drop event) throws ResourceException {
		Player player = entities.getEntity(0);
		player.getComponent(Inventory.class).removeItem(event.getItem());
		Shape shape = player.getComponent(Shape.class);
		RMap map = resources.getResource("maps", event.getMap());
		map.addEntity(event.getItem(), shape.getX(), shape.getY());
		Item item = entities.getEntity(event.getItem());
		item.getComponent(Shape.class).setPosition(shape.getX(), shape.getY(), shape.getZ());
		String id = item.getComponent(Item.Resource.class).getResource().id;
		bus.post(new UpdateEvent.Item(item.uid, id, map.id, shape.getX(), shape.getY(), shape.getZ()));
	}
	
	@Subscribe
	private void pick(InventoryEvent.Pick event) throws ResourceException {
		RMap map = resources.getResource("maps", event.getMap());
		map.removeEntity(event.getItem());
		Player player = entities.getEntity(0);
		player.getComponent(Inventory.class).addItem(event.getItem());

		Item item = entities.getEntity(event.getItem());
		bus.post(new UpdateEvent.Remove(item.uid, event.getMap()));
	}
}
