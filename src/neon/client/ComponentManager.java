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

package neon.client;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.Table;

import neon.common.entity.components.Component;

/**
 * A table containing all components for all currently loaded entities.
 * 
 * @author mdriesen
 *
 */
public final class ComponentManager {
	private final Table<Long, Class<? extends Component>, Component> components = HashBasedTable.create();
	
	/**
	 * Puts a component in the table.
	 * 
	 * @param component	the {@code Component} to add
	 */
	public void putComponent(Component component) {
		components.put(component.getEntity(), component.getClass(), component);
	}
	
	/**
	 * Removes an entity from the table.
	 * 
	 * @param uid	the uid of an entity
	 */
	public void removeEntity(long uid) {
		components.row(uid).clear();
	}
	
	/**
	 * 
	 * @param uid	the uid of an entity
	 * @param type	the type of a component
	 * @return	a component of the given type for the given entity
	 */
	public <R extends Component> R getComponent(long uid, Class<R> type) {
		return type.cast(components.get(uid, type));
	}
	
	/**
	 * Checks whether an entity has a certain component type.
	 * 
	 * @param uid	the uid of an entity
	 * @param type	the type of the component
	 * @return
	 */
	public boolean hasComponent(long uid, Class<?> type) {
		return components.contains(uid, type);
	}
	
	/**
	 * Returns all components of an entity.
	 * 
	 * @param uid
	 * @return	an immutable {@code ClassToInstanceMap} of {@code Component}s
	 */
	public ClassToInstanceMap<Component> getComponents(long uid) {
		return ImmutableClassToInstanceMap.copyOf(components.rowMap().get(uid));
	}
}
