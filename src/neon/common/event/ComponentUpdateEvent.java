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

package neon.common.event;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import neon.entity.components.Component;

public class ComponentUpdateEvent extends NeonEvent {
	private final static Gson gson = new Gson();
	
	private final String component;
	private final String type;
	
	public ComponentUpdateEvent(Component component) {
		this.component = gson.toJson(component);
		type = component.getClass().getCanonicalName();
		System.out.println(this.component);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent() throws JsonSyntaxException, ClassNotFoundException {
		return (T) gson.fromJson(component, Class.forName(type));
	}
}
