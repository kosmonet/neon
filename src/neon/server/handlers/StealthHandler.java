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

package neon.server.handlers;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Lock;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.DoorEvent;
import neon.common.event.StealthEvent;
import neon.common.resources.RItem;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

public final class StealthHandler {
	private static final Random random = new Random();
	private static final long PLAYER_UID = 0;
	
	private final EntityManager entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public StealthHandler(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.resources = resources;
		this.entities = entities;
		this.bus = bus;
	}
	
	@Subscribe
	private void onPickPocket(StealthEvent.Pick event) {
		Entity victim = entities.getEntity(event.victim);
		Inventory victimInventory = victim.getComponent(Inventory.class);
		Equipment victimEquipment = victim.getComponent(Equipment.class);
		// filter the items so no equipped item can be stolen
		Collection<Long> items = victimInventory.getItems().stream()
				.filter(uid -> !victimEquipment.hasEquipped(uid)).collect(Collectors.toList());

		if (items.isEmpty()) {
			bus.post(new StealthEvent.Empty());
		} else {
			long item = items.stream().skip(random.nextInt(items.size())).findFirst().get();
			victimInventory.removeItem(item);
			Inventory playerInventory = entities.getEntity(PLAYER_UID).getComponent(Inventory.class);
			playerInventory.addItem(item);
			bus.post(new ComponentUpdateEvent(victimInventory));
			bus.post(new ComponentUpdateEvent(playerInventory));
			bus.post(new StealthEvent.Stolen());
		}
	}
	
	@Subscribe
	private void onLockpick(StealthEvent.Unlock event) {
		Entity item = entities.getEntity(event.lock);
		item.getComponent(Lock.class).unlock();
		bus.post(new ComponentUpdateEvent(item.getComponent(Lock.class)));
		bus.post(new StealthEvent.Unlocked());		
	}
	
	@Subscribe
	private void onDoorOpen(DoorEvent.Open event) throws ResourceException {
		Entity door = entities.getEntity(event.door);
		DoorInfo info = door.getComponent(DoorInfo.class);
		info.open();
		RItem.Door resource = resources.getResource("items", door.getComponent(ItemInfo.class).id);
		Graphics graphics = new Graphics(door.uid, resource.glyph, resource.color);
		door.setComponent(graphics);
		bus.post(new ComponentUpdateEvent(graphics));
		bus.post(new ComponentUpdateEvent(info));
	}
}
