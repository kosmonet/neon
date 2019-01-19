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

import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import neon.common.resources.Slot;

/**
 * A component that keeps track of equipped items.
 * 
 * @author mdriesen
 */
public class Equipment implements Component {
	private final long uid;
	private final Map<Slot, Long> equipped = new EnumMap<>(Slot.class);

	/**
	 * Initialize this component.
	 * 
	 * @param uid	the uid of the entity this equipment belongs to
	 */
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
	 * @param uid	the uid of an item
	 * @return	{@code true} if the item is equipped, {@code false} otherwise
	 */
	public boolean hasEquipped(long uid) {
		return equipped.containsValue(uid);
	}
	
	/**
	 * Checks whether an item is equipped in a slot.
	 * 
	 * @param slot	an equipment {@code Slot}
	 * @return	{@code true} if an item is equipped in the given slot, {@code false} otherwise
	 */
	public boolean hasEquipped(Slot slot) {
		return equipped.containsKey(slot);
	}
	
	/**
	 * Unequips an item.
	 * 
	 * @param uid	the uid of an item
	 */
	public void unequip(Long uid) {
		equipped.values().removeIf(uid::equals);
	}
	
	/**
	 * Unequips the item in an equipment slot.
	 * 
	 * @param slot	an equipment {@code Slot}
	 */
	public void unequip(Slot slot) {
		unequip(equipped.get(slot));
	}
	
	/**
	 * Equips an item to a slot.
	 * 
	 * @param slot	an equipment {@code Slot}
	 * @param uid	the uid of an item
	 */
	public void equip(Slot slot, long uid) {
		equipped.put(slot, uid);
	}
	
	/**
	 * Returns the item equipped in a slot.
	 * 
	 * @param slot	an equipment {@code Slot}
	 * @return	the uid of the item in the given equipment slot.
	 */
	public long getEquippedItem(Slot slot) {
		return equipped.get(slot);
	}
	
	/**
	 * Returns all the equipped items.
	 * 
	 * @return	an {@code Iterable<Long>} of item uid's
	 */
	public Iterable<Long> getEquippedItems() {
		return ImmutableList.copyOf(equipped.values());
	}
}
