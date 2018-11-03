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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.Inventory;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.StealthEvent;
import neon.server.entity.EntityManager;

public final class StealthHandler {
	private static final Random random = new Random();
	private static final long PLAYER_UID = 0;
	
	private final EntityManager entities;
	private final EventBus bus;
	
	public StealthHandler(EntityManager entities, EventBus bus) {
		this.entities = entities;
		this.bus = bus;
	}
	
	@Subscribe
	private void onPickPocket(StealthEvent.Pick event) {
		Entity victim = entities.getEntity(event.victim);
		Inventory victimInventory = victim.getComponent(Inventory.class);
		Collection<Long> items = victimInventory.getItems();

		if (items.isEmpty()) {
			bus.post(new StealthEvent.Empty());
		} else {
			long item = items.stream().skip(random.nextInt(items.size())).findFirst().get();
			victimInventory.removeItem(item);
			Inventory playerInventory = entities.getEntity(PLAYER_UID).getComponent(Inventory.class);
			playerInventory.addItem(item);
			bus.post(new ComponentUpdateEvent(victimInventory));
			bus.post(new ComponentUpdateEvent(playerInventory));
			bus.post(new StealthEvent.Success());
		}
	}
}
