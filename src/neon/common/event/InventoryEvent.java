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

package neon.common.event;

import java.util.Collection;

public abstract class InventoryEvent extends NeonEvent {
	public static class Update extends InventoryEvent {
		private final long[] items;
		private final long[] equiped;
		private final int money;
		
		public Update(Collection<Long> items, Collection<Long> equiped, int money) {
			this.items = items.stream().mapToLong(Long::longValue).toArray();
			this.equiped = equiped.stream().mapToLong(Long::longValue).toArray();
			this.money = money;
		}
		
		public long[] getItems() {
			return items;
		}
		
		public long[] getEquipedItems() {
			return equiped;
		}
		
		public int getMoney() {
			return money;
		}
	}
	
	public static class Drop extends InventoryEvent {
		private final long uid;
		private final String id;
		
		public Drop(long uid, String id) {
			this.uid = uid;
			this.id = id;
		}
		
		public long getItem() {
			return uid;
		}
		
		public String getMap() {
			return id;
		}
	}

	public static class Pick extends InventoryEvent {
		private final long uid;
		private final String map;
		
		public Pick(long uid, String map) {
			this.uid = uid;
			this.map = map;
		}
		
		public long getItem() {
			return uid;
		}
		
		public String getMap() {
			return map;
		}
	}

	public static class Equip extends InventoryEvent {
		public final long uid;
		
		public Equip(long uid) {
			this.uid = uid;
		}
	}
}
