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

package neon.server.entity;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.scene.paint.Color;
import neon.common.entity.Entity;
import neon.common.entity.components.Component;
import neon.common.net.ColorAdapter;

/**
 * A json (de)serializer that is used to save/load entities on/from disk.
 * 
 * @author mdriesen
 *
 */
public class EntityAdapter implements JsonSerializer<Entity>, JsonDeserializer<Entity> {
	private static final GsonBuilder BUILDER = new GsonBuilder()
			.registerTypeAdapter(Color.class, new ColorAdapter())
			.enableComplexMapKeySerialization();
	private static final Gson GSON = BUILDER.create();
	
	@Override
	public Entity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		long uid = element.getAsJsonObject().get("uid").getAsLong();
		element.getAsJsonObject().remove("uid");
		Entity entity = new Entity(uid);
		
		for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
			try {
				Component component = Component.class.cast(GSON.fromJson(entry.getValue(), Class.forName(entry.getKey())));
				entity.setComponent(component);
			} catch (ClassNotFoundException e) {
				throw new AssertionError("Unknown class in entity " + entity.uid + ": " + entry.getKey());
			}
		}
		
		return entity;
	}

	@Override
	public JsonElement serialize(Entity entity, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("uid", entity.uid);
		
		for (Component component: entity.getComponents()) {
			String cType = component.getClass().getTypeName();
			object.add(cType, GSON.toJsonTree(component));
		}
		
		return object;
	}
}
