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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import neon.entity.components.Component;

public class ComponentManager {
	private final Table<Long, Class<? extends Component>, Component> components = HashBasedTable.create();
	
	public <T extends Component> void putComponent(long uid, T component) {
		components.put(uid, component.getClass(), component);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent(long uid, Class<T> type) {
		return (T) components.get(uid, type);
	}
}
