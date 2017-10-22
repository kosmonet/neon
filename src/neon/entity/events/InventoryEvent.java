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

package neon.entity.events;

import java.util.ArrayList;
import java.util.Collection;

import neon.common.event.ClientEvent;
import neon.entity.entities.Item;

public class InventoryEvent implements ClientEvent {
	private final ArrayList<Item> items;
	
	public InventoryEvent(Collection<Item> items) {
		this.items = new ArrayList<>(items);
	}
	
	public Collection<Item> getItems() {
		return items;
	}
}
