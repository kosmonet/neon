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

package neon.entity.components;

import neon.entity.Slot;

public class Weapon implements Component {
	private final long uid;
	private final String damage;
	
	public Weapon(long uid, String damage) {
		this.uid = uid;
		this.damage = damage;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public String getDamage() {
		return damage;
	}
	
	public Slot getSlot() {
		return Slot.WEAPON;
	}
}
