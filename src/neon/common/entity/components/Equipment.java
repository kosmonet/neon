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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import neon.common.resources.Slot;

/**
 * A component that keeps track of equipped items.
 * @author mdriesen
 *
 */
public class Equipment implements Component {
	private final long uid;
	private final Map<Slot, Long> equipped = new EnumMap<>(Slot.class);

	public Equipment(long uid) {
		this.uid = uid;
	}
	
	@Override
	public long getEntity() {
		return uid;
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
