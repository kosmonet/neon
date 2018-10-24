/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.EntityProvider;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Weapon;
import neon.common.entity.entities.Creature;
import neon.common.entity.entities.Item;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.InventoryEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

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
	private void onItemDrop(InventoryEvent.Drop event) throws ResourceException {
		Creature player = entities.getEntity(0);
		Inventory inventory = player.getComponent(Inventory.class);
		inventory.removeItem(event.getItem());
		Shape shape = player.getComponent(Shape.class);
		RMap map = resources.getResource("maps", event.getMap());
		map.addEntity(event.getItem(), shape.getX(), shape.getY());
		Item item = entities.getEntity(event.getItem());
		item.getComponent(Shape.class).setPosition(shape.getX(), shape.getY(), shape.getZ());
		bus.post(new ComponentUpdateEvent(inventory));
		bus.post(new UpdateEvent.Move(item.uid, map.id, shape.getX(), shape.getY(), shape.getZ()));
	}
	
	@Subscribe
	private void onItemPick(InventoryEvent.Pick event) throws ResourceException {
		RMap map = resources.getResource("maps", event.getMap());
		map.removeEntity(event.getItem());
		Creature player = entities.getEntity(0);
		Inventory inventory = player.getComponent(Inventory.class);
		inventory.addItem(event.getItem());

		bus.post(new UpdateEvent.Remove(event.getItem(), map.id));
		bus.post(new ComponentUpdateEvent(inventory));
	}
	
	@Subscribe
	private void onItemEquip(InventoryEvent.Equip event) throws ResourceException {
		Creature player = entities.getEntity(0);
		Item item = entities.getEntity(event.uid);
		Inventory inventory = player.getComponent(Inventory.class);
		
		if (inventory.getItems().contains(event.uid) && item.hasComponent(Clothing.class)) {
			Clothing cloth = item.getComponent(Clothing.class);
			if (inventory.hasEquiped(event.uid)) {
				inventory.unEquip(cloth.getSlot());
			} else {
				inventory.equip(cloth.getSlot(), event.uid);				
			}
		}
		
		if (inventory.getItems().contains(event.uid) && item.hasComponent(Weapon.class)) {
			Weapon weapon = item.getComponent(Weapon.class);
			if (inventory.hasEquiped(event.uid)) {
				inventory.unEquip(weapon.getSlot());
			} else {
				inventory.equip(weapon.getSlot(), event.uid);				
			}
		}
		
		bus.post(new ComponentUpdateEvent(inventory));
	}
}
