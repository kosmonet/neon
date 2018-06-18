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
import neon.common.event.NeonEvent;
import neon.entity.EntityProvider;
import neon.entity.components.InventoryComponent;
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
	
	public InventoryHandler(EntityProvider entities, EventBus bus) {
		this.bus = bus;
		this.entities = entities;
	}
	
	@Subscribe
	private void postInventory(NeonEvent.Inventory event) {
		Player player = entities.getEntity(0);
		ArrayList<Long> items = new ArrayList<>();
		InventoryComponent inventory = player.getComponent("inventory");
		items.addAll(inventory.getItems());
		bus.post(new InventoryEvent(items));
	}
}
