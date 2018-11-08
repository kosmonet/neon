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

package neon.common.net;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.scene.paint.Color;

public final class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
	@Override
	public Color deserialize(JsonElement element, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		double red = element.getAsJsonObject().get("red").getAsDouble();
		double green = element.getAsJsonObject().get("green").getAsDouble();
		double blue = element.getAsJsonObject().get("blue").getAsDouble();
		double opacity = element.getAsJsonObject().get("alpha").getAsDouble();
		return new Color(red, green, blue, opacity);
	}

	@Override
	public JsonElement serialize(Color color, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("red", (float) color.getRed());
		object.addProperty("green", (float) color.getGreen());
		object.addProperty("blue", (float) color.getBlue());
		object.addProperty("alpha", (float) color.getOpacity());
		return object;
	}
}