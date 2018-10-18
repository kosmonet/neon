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

package neon.entity.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import neon.entity.Slot;

public class Inventory implements Component {
	private final long uid;
	private final Collection<Long> items = new ArrayList<Long>();
//	private final EnumMap<Slot, Long> equiped = new EnumMap<>(Slot.class);
	private final LinkedHashMap<Slot, Long> equiped = new LinkedHashMap<>();
	
	private int money = 0;
	
	public Inventory(long uid) {
		this.uid = uid;
	}
	
	public long getEntity() {
		return uid;
	}
	
	public void addItem(long uid) {
		items.add(uid);
	}
	
	public void addItems(Collection<Long> items) {
		this.items.addAll(items);
	}
	
	public void removeItem(long uid) {
		// unequip before removing
		if (equiped.containsValue(uid)) {
			for (Map.Entry<Slot, Long> entry : equiped.entrySet()) {
				if (entry.getValue() == uid) {
					unEquip(entry.getKey());
				}
			}
		}
		
		items.remove(uid);
	}
	
	public Collection<Long> getItems() {
		return items;
	}
	
	public void addMoney(int amount) {
		money += amount;
	}
	
	public int getMoney() {
		return money;
	}
	
	public boolean hasEquiped(long uid) {
		return equiped.containsValue(uid);
	}
	
	public boolean hasEquiped(Slot slot) {
		return equiped.containsKey(slot);
	}
	
	public void unEquip(Slot slot) {
		equiped.remove(slot);
	}
	
	public void equip(Slot slot, long uid) {
		equiped.put(slot, uid);
	}
	
	public long getEquipedItem(Slot slot) {
		return equiped.get(slot);
	}
	
	public Collection<Long> getEquipedItems() {
		return equiped.values();
	}
}
