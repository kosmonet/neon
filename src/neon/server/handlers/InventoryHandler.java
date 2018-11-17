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

import neon.common.entity.Entity;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.Currency;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.InventoryEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.ResourceException;
import neon.server.Configuration;
import neon.server.entity.EntityManager;
import neon.server.entity.Map;
import neon.systems.combat.Weapon;

/**
 * This class handles the inventory-related server bits.
 * 
 * @author mdriesen
 *
 */
public final class InventoryHandler {
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final EntityManager entities;
	private final Configuration config;
	
	public InventoryHandler(EntityManager entities, EventBus bus, Configuration config) {
		this.bus = bus;
		this.entities = entities;
		this.config = config;
	}
	
	/**
	 * Stores an item in a container.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onItemStore(InventoryEvent.Store event) {
		Entity player = entities.getEntity(PLAYER_UID);
		Entity container = entities.getEntity(event.container);
		
		// make sure the item is no longer equipped
		Equipment equipment = player.getComponent(Equipment.class);
		equipment.unequip(event.item);
		// then actually remove the item
		Inventory inventory = player.getComponent(Inventory.class);
		inventory.removeItem(event.item);
		// and store it in the container
		Inventory contents = container.getComponent(Inventory.class);
		contents.addItem(event.item);
		
		// let the client know
		bus.post(new ComponentUpdateEvent(inventory));
		bus.post(new ComponentUpdateEvent(equipment));
		bus.post(new ComponentUpdateEvent(contents));
	}
	
	/**
	 * Makes the player drop an item on the map.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void onItemDrop(InventoryEvent.Drop event) throws ResourceException {
		Entity player = entities.getEntity(PLAYER_UID);
		// make sure the item is no longer equipped
		Equipment equipment = player.getComponent(Equipment.class);
		equipment.unequip(event.item);
		// then actually remove the item
		Inventory inventory = player.getComponent(Inventory.class);
		inventory.removeItem(event.item);
		
		Shape shape = player.getComponent(Shape.class);
		Map map = config.getCurrentMap();
		map.addEntity(event.item, shape.getX(), shape.getY());
		Entity item = entities.getEntity(event.item);
		item.getComponent(Shape.class).setPosition(shape.getX(), shape.getY(), shape.getZ());
		
		// let the client know
		bus.post(new ComponentUpdateEvent(inventory));
		bus.post(new ComponentUpdateEvent(equipment));
		bus.post(new UpdateEvent.Move(item.uid, map.getUid(), shape.getX(), shape.getY(), shape.getZ()));
	}
	
	/**
	 * Makes the player pick up an item from the map.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void onItemPick(InventoryEvent.Pick event) throws ResourceException {
		Map map = config.getCurrentMap();
		map.removeEntity(event.item);
		bus.post(new UpdateEvent.Remove(event.item, map.getUid()));
		
		Entity player = entities.getEntity(PLAYER_UID);
		Inventory inventory = player.getComponent(Inventory.class);
		Entity item = entities.getEntity(event.item);
		
		// currency is not added to the inventory as an item, but as money
		if (item.hasComponent(Currency.class)) {
			inventory.addMoney(item.getComponent(ItemInfo.class).price);
			entities.removeEntity(event.item);
			bus.post(new UpdateEvent.Destroy(event.item));
		} else {
			inventory.addItem(event.item);
		}
		
		bus.post(new ComponentUpdateEvent(inventory));
	}
	
	/**
	 * Makes the player pick up an item from the map.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void onItemTake(InventoryEvent.Take event) throws ResourceException {
		Entity player = entities.getEntity(PLAYER_UID);
		Entity container = entities.getEntity(event.container);
		Entity item = entities.getEntity(event.item);

		// remove item from the container
		Inventory contents = container.getComponent(Inventory.class);
		contents.removeItem(event.item);
		// then add the item to the player inventory
		Inventory inventory = player.getComponent(Inventory.class);
		// currency is not added to the inventory as an item, but as money
		if (item.hasComponent(Currency.class)) {
			inventory.addMoney(item.getComponent(ItemInfo.class).price);
			entities.removeEntity(event.item);
			bus.post(new UpdateEvent.Destroy(event.item));
		} else {
			inventory.addItem(event.item);
		}
		
		// let the client know
		bus.post(new ComponentUpdateEvent(inventory));
		bus.post(new ComponentUpdateEvent(contents));

	}
	
	/**
	 * Unequips an item on the player.
	 * 
	 * @param event
	 */
	@Subscribe 
	private void onItemUnequip(InventoryEvent.Unequip event) {
		Entity player = entities.getEntity(PLAYER_UID);
		Equipment equipment = player.getComponent(Equipment.class);
		equipment.unequip(event.uid);
		bus.post(new ComponentUpdateEvent(equipment));
	}
	
	/**
	 * Equips an item on the player.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void onItemEquip(InventoryEvent.Equip event) throws ResourceException {
		Entity player = entities.getEntity(PLAYER_UID);
		Entity item = entities.getEntity(event.uid);
		Inventory inventory = player.getComponent(Inventory.class);
		Equipment equipment = player.getComponent(Equipment.class);
		
		// clothing (and armor) keeps track of what slot they cover
		if (inventory.getItems().contains(event.uid) && item.hasComponent(Clothing.class)) {
			Clothing cloth = item.getComponent(Clothing.class);
			equipment.equip(cloth.getSlot(), event.uid);				
		}
		
		// weapons can be equipped in the hand of choice
		if (inventory.getItems().contains(event.uid) && item.hasComponent(Weapon.class)) {
			equipment.equip(event.slot.get(), event.uid);				
		}
		
		bus.post(new ComponentUpdateEvent(equipment));
	}
}
