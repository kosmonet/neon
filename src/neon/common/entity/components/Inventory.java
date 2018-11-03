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

package neon.common.entity.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import neon.common.entity.Slot;

/**
 * The inventory of a creature.
 * 
 * @author mdriesen
 *
 */
public final class Inventory implements Component {
	private final long uid;
	private final List<Long> items = new ArrayList<Long>();
	private final HashMap<Slot, Long> equiped = new HashMap<>();
	
	private int money = 0;
	
	/**
	 * Creates an inventory for the creature with the given uid.
	 * 
	 * @param uid
	 */
	public Inventory(long uid) {
		this.uid = uid;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Inventory:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	/**
	 * Adds an item to this inventory.
	 * 
	 * @param uid	the uid of the item to add
	 */
	public void addItem(long uid) {
		items.add(uid);
	}
	
	/**
	 * Adds all items of a given collection to this inventory.
	 * 
	 * @param items
	 */
	public void addItems(Collection<Long> items) {
		this.items.addAll(items);
	}
	
	/**
	 * Removes an item from this inventory.
	 * 
	 * @param uid	the uid of the item to remove
	 */
	public void removeItem(long uid) {
		// unequip before removing
		unequip(uid);
		items.remove(uid);
	}
	
	/**
	 * Returns a collection of all items in an inventory.
	 * 
	 * @return
	 */
	public Collection<Long> getItems() {
		return Collections.unmodifiableList(items);
	}
	
	/**
	 * Adds money to this inventory.
	 * 
	 * @param amount	the amount of money to add
	 */
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
	
	public void unequip(Long uid) {
		equiped.values().removeIf(uid::equals);
	}
	
	public void unequip(Slot slot) {
		unequip(equiped.get(slot));
	}
	
	public void equip(Slot slot, long uid) {
		equiped.put(slot, uid);
	}
	
	public long getEquipedItem(Slot slot) {
		return equiped.get(slot);
	}
	
	public Collection<Long> getEquipedItems() {
		return Collections.unmodifiableCollection(equiped.values());
	}
}
