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

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import javafx.scene.paint.Color;
import neon.entity.components.Component;

public class ComponentUpdateEvent extends NeonEvent {
	private final static GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Color.class, new ColorAdapter());
	private final static Gson gson = builder.create();

	private final String component;
	private final String type;
	
	public ComponentUpdateEvent(Component component) {
		this.component = gson.toJson(component);
		type = component.getClass().getTypeName();
		System.out.println(this.component);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent() throws JsonSyntaxException, ClassNotFoundException {
		return (T) gson.fromJson(component, Class.forName(type));
	}
	
	private static class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
		@Override
		public Color deserialize(JsonElement element, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			double red = element.getAsJsonObject().get("red").getAsDouble();
			double green = element.getAsJsonObject().get("green").getAsDouble();
			double blue = element.getAsJsonObject().get("blue").getAsDouble();
			double opacity = element.getAsJsonObject().get("opacity").getAsDouble();
			return new Color(red, green, blue, opacity);
		}

		@Override
		public JsonElement serialize(Color color, Type type, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("red", color.getRed());
			object.addProperty("green", color.getGreen());
			object.addProperty("blue", color.getBlue());
			object.addProperty("opacity", color.getOpacity());
			return object;
		}
	}
}
