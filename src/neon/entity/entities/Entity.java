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

package neon.entity.entities;

import java.util.HashMap;

import neon.entity.components.Component;

public abstract class Entity {
	public final long uid;
	protected final HashMap<String, Component> components = new HashMap<>();
	
	public Entity(long uid) {
		this.uid = uid;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent(String component) {
		return (T) components.get(component);
	}
	
	public boolean hasComponent(String component) {
		return components.containsKey(component);
	}
}
