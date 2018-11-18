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

import java.util.Optional;

import neon.common.resources.Slot;

public abstract class InventoryEvent extends NeonEvent {
	/**
	 * Event to indicate the player dropped something on the ground.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Drop extends InventoryEvent {
		public final long item;
		public final String map;
		
		public Drop(long item, String map) {
			this.item = item;
			this.map = map;
		}
	}

	/**
	 * Event to indicate the player stored something in a container.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Store extends InventoryEvent {
		public final long item;
		public final long container;
		
		public Store(long item, long container) {
			this.item = item;
			this.container = container;
		}
	}

	/**
	 * Event to indicate the player picked something up from the ground.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Pick extends InventoryEvent {
		public final long item;
		public final String map;
		
		public Pick(long item, String map) {
			this.item = item;
			this.map = map;
		}
	}

	/**
	 * Event to indicate the player took something from a container.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Take extends InventoryEvent {
		public final long item;
		public final long container;
		
		public Take(long item, long container) {
			this.item = item;
			this.container = container;
		}
	}

	public static final class Unequip extends InventoryEvent {
		public final long uid;
		
		public Unequip(long uid) {
			this.uid = uid;
		}		
	}
	
	public static final class Equip extends InventoryEvent {
		public final long uid;
		public final Optional<Slot> slot;
		
		public Equip(long uid) {
			this.uid = uid;
			slot = Optional.empty();
		}

		public Equip(long uid, Slot slot) {
			this.uid = uid;
			this.slot = Optional.ofNullable(slot);
		}
	}
}
