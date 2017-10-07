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

import java.util.ArrayList;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.ClientEvent;
import neon.common.event.ServerEvent;
import neon.entity.entities.Item;
import neon.entity.entities.Player;
import neon.server.EntityTracker;

/**
 * This system handles the inventory-related server bits.
 * 
 * @author mdriesen
 *
 */
public class InventorySystem {
	private final EventBus bus;
	private final EntityTracker entities;
	
	public InventorySystem(EntityTracker entities, EventBus bus) {
		this.bus = bus;
		this.entities = entities;
	}
	
	@Subscribe
	private void getInventory(ServerEvent.Inventory event) {
		Player player = entities.getEntity(0);
		ArrayList<Item> items = new ArrayList<>();
		for (long uid : player.inventory.getItems()) {
			items.add(entities.getEntity(uid));
		}
		
		bus.post(new ClientEvent.Inventory(items));
	}
}