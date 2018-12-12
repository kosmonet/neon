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

/**
 * A lock that can be placed on e.g. doors or containers.
 * 
 * @author mdriesen
 *
 */
public final class Lock implements Component {
	private final long uid;
	
	private boolean locked = true;
	
	/**
	 * Initializes a new lock.
	 * 
	 * @param uid	the uid of the entity this lock belongs to
	 */
	public Lock(long uid) {
		this.uid = uid;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Lock:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	/**
	 * Locks this lock.
	 */
	public void lock() {
		locked = true;
	}
	
	/**
	 * Unlocks this lock.
	 */
	public void unlock() {
		locked = false;
	}
}
