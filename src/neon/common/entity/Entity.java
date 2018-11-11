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

package neon.common.entity;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import neon.common.entity.components.Component;

/**
 * An entity represents a set of components.
 * 
 * @author mdriesen
 *
 */
public class Entity {
	public final long uid;
	private final ClassToInstanceMap<Component> components = MutableClassToInstanceMap.create();
	
	/**
	 * Initialize a new entity.
	 * 
	 * @param uid	the unique identifier of this entity
	 */
	public Entity(long uid) {
		this.uid = uid;
	}
	
	@Override 
	public String toString() {
		// create a string in module:map:entity format
		return "Entity:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	/**
	 * 
	 * @param component
	 * @return	the component of the given type
	 */
	public <T extends Component> T getComponent(Class<T> type) {
		return components.getInstance(type);
	}
	
	/**
	 * 
	 * @param component
	 * @return	whether this entity has a component of the given type
	 */
	public boolean hasComponent(Class<? extends Component> type) {
		return components.containsKey(type);
	}
	
	/**
	 * Adds a component to this entity.
	 * 
	 * @param component
	 */
	public <T extends Component> void setComponent(T component) {
		components.put(component.getClass(), component);
	}
	
	/**
	 * Removes a component.
	 * 
	 * @param type
	 */
	public void removeComponent(Class<? extends Component> type) {
		components.remove(type);
	}
}
