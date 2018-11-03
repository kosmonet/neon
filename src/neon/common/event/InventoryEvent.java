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

import com.google.common.base.Optional;

import neon.common.entity.Slot;

public abstract class InventoryEvent extends NeonEvent {
	public static final class Drop extends InventoryEvent {
		public final long uid;
		public final String map;
		
		public Drop(long uid, String map) {
			this.uid = uid;
			this.map = map;
		}
	}

	public static final class Pick extends InventoryEvent {
		public final long uid;
		public final String map;
		
		public Pick(long uid, String map) {
			this.uid = uid;
			this.map = map;
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
			slot = Optional.absent();
		}

		public Equip(long uid, Slot slot) {
			this.uid = uid;
			this.slot = Optional.fromNullable(slot);
		}
	}
}
