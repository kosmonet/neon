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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
	private final Map<Slot, Long> equipped = new EnumMap<>(Slot.class);
	
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
	
	/**
	 * Returns the creature's total money.
	 * 
	 * @return
	 */
	public int getMoney() {
		return money;
	}
	
	/**
	 * Checks whether an item is equipped.
	 * 
	 * @param uid
	 * @return
	 */
	public boolean hasEquipped(long uid) {
		return equipped.containsValue(uid);
	}
	
	/**
	 * Checks whether an item is equipped in the given slot.
	 * @param slot
	 * @return
	 */
	public boolean hasEquipped(Slot slot) {
		return equipped.containsKey(slot);
	}
	
	/**
	 * Unequip an item.
	 * 
	 * @param uid
	 */
	public void unequip(Long uid) {
		equipped.values().removeIf(uid::equals);
	}
	
	/**
	 * Unequip the item in the given slot.
	 * 
	 * @param slot
	 */
	public void unequip(Slot slot) {
		unequip(equipped.get(slot));
	}
	
	/**
	 * Equip an item to a slot.
	 * 
	 * @param slot
	 * @param uid
	 */
	public void equip(Slot slot, long uid) {
		equipped.put(slot, uid);
	}
	
	/**
	 * 
	 * @param slot
	 * @return	the item in the given equipment slot.
	 */
	public long getEquipedItem(Slot slot) {
		return equipped.get(slot);
	}
	
	/**
	 * Returns a collection of all the equipped items.
	 * 
	 * @return
	 */
	public Collection<Long> getEquippedItems() {
		return Collections.unmodifiableCollection(equipped.values());
	}
}
