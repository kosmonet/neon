/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

import neon.common.entity.Slot;

public final class Clothing implements Component {
	private final long uid;
	private final Slot slot;
	
	public Clothing(long uid, Slot slot) {
		this.uid = uid;
		this.slot = slot;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Clothing:");
		// create a string in module:map:entity format
		builder.append((uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl));
		builder.append("[slot:" + slot + "]");
		return builder.toString();
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public Slot getSlot() {
		return slot;
	}
}
