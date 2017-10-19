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

package neon.entity.components;

import java.util.Observable;

/**
 * A component that represents the shape (including position) of an entity.
 * 
 * @author mdriesen
 *
 */
public class ShapeComponent extends Observable implements Component {
	private final long uid;
	
	private int x, y, z;
	
	public ShapeComponent(long uid) {
		this.uid = uid;
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
		setChanged();
		notifyObservers();
	}

	public void setX(int x) {
		this.x = x;
		setChanged();
		notifyObservers();
	}

	public void setY(int y) {
		this.y = y;
		setChanged();
		notifyObservers();
	}

	public void setZ(int z) {
		this.z = z;
		setChanged();
		notifyObservers();
	}
}
