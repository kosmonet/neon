/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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
 * A component that represents the shape (including position) of an entity.
 * 
 * @author mdriesen
 *
 */
public class Shape implements Component {
	private final long uid;
	
	private int x, y, z;
	
	public Shape(long uid) {
		this.uid = uid;
	}
	
	public Shape(long uid, int x, int y, int z) {
		this.uid = uid;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Shape:");
		// create a string in module:map:entity format
		builder.append((uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl));
		builder.append("[x:" + x + ", y:" + y + ", z:" + z + "]");
		return builder.toString();
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	/**
	 * Sets the position of the entity this component belongs to.
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setZ(int z) {
		this.z = z;
	}
}
