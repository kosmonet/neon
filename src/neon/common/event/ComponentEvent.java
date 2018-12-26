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
import com.google.gson.GsonBuilder;

import javafx.scene.paint.Color;
import neon.common.entity.components.Component;
import neon.common.net.ColorAdapter;

/**
 * An event to signal a change in a component.
 * 
 * @author mdriesen
 *
 */
public final class ComponentEvent extends NeonEvent {
	private static final GsonBuilder builder = new GsonBuilder()
			.registerTypeAdapter(Color.class, new ColorAdapter())
			.enableComplexMapKeySerialization()
			.disableHtmlEscaping();
	private static final Gson gson = builder.create();

	private final String component;
	private final String type;
	
	public ComponentEvent(Component component) {
		this.component = gson.toJson(component);
		type = component.getClass().getTypeName();
		System.out.println(this.component);
	}
	
	/**
	 * Returns the component that was changed.
	 * 
	 * @return	a {@code Component}
	 * @throws ClassNotFoundException	if the serialized class was not a component
	 */
	public Component getComponent() throws ClassNotFoundException {
		return Component.class.cast(gson.fromJson(component, Class.forName(type)));
	}
}
