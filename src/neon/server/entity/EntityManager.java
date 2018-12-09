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

package neon.server.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import neon.common.entity.Entity;
import neon.common.files.JsonTranslator;
import neon.common.files.NeonFileSystem;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

/**
 * This class manages all loaded entities in the current game (and by extension
 * all loaded modules and maps). 
 * 
 * @author mdriesen
 *
 */
public final class EntityManager {
	private static final Logger logger = Logger.getGlobal();
	private static final GsonBuilder builder = new GsonBuilder()
			.registerTypeAdapter(Entity.class, new EntityAdapter());
	private static final Gson gson = builder.create();
	private static final JsonTranslator translator = new JsonTranslator();

	private final Cache<Long, Entity> entities = CacheBuilder.newBuilder().removalListener(new EntityListener()).softValues().build();
	private final HashMap<Class<?>, EntityBuilder> builders = new HashMap<>();
	private final NeonFileSystem files;
	private final Cache<String, Map> maps = CacheBuilder.newBuilder().removalListener(new MapListener()).softValues().build();
	private final HashBiMap<String, Short> uids = HashBiMap.create();
	private final HashSet<Module> modules = new HashSet<>();
	private final MapLoader loader;
	
	public EntityManager(NeonFileSystem files, ResourceManager resources) {
		this.files = files;
		loader = new MapLoader(files, resources, this);
	}
	
	/**
	 * Returns the entity with the given uid. If the entity is not present, an
	 * {@code IllegalArgumentException} is thrown.
	 * 
	 * @param uid
	 * @return
	 */
	public Entity getEntity(long uid) {
		try {
			return entities.get(uid, () -> loadEntity(uid));
		} catch (ExecutionException e) {
			throw new IllegalArgumentException("No entity with uid <" + uid + "> found", e);
		}
	}
	
	/**
	 * Removes an entity from the cache.
	 * 
	 * @param uid
	 */
	public void removeEntity(long uid) {
		entities.invalidate(uid);
	}
	
	/**
	 * Creates a new entity from the given resource and uid. The new entity
	 * is added to the cache.
	 * 
	 * @param uid
	 * @param resource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Entity createEntity(long uid, Resource resource) {
		Entity entity = builders.get(resource.getClass()).build(uid, resource);
		entities.put(uid, entity);		
		return entity;
	}
	
	/**
	 * Adds an entity builder.
	 * 
	 * @param type
	 * @param builder
	 */
	public <T extends Resource> void addBuilder(Class<T> type, EntityBuilder<? super T> builder) {
		builders.put(type, builder);
	}
	
	/**
	 * Calculates the full map uid given the module name and the base uid
	 * of the map within the module.
	 * 
	 * @param base
	 * @param module
	 * @return
	 */
	public int getMapUID(short base, String module) {
		return ((int) getModuleUID(module) << 16) | base;
	}
	
	public Map getMap(String id) throws IOException, ResourceException {
		// load the map if it didn't exist yet
		if (!maps.asMap().containsKey(id)) {
			maps.put(id,  loader.loadMap(id));
		}
		
		return maps.getIfPresent(id);
	}
	
	/**
	 * 
	 * @param entity
	 * @return	the uid of the module an entity belongs to
	 */
	short getModuleUID(long entity) {
		return (short) (entity >>> 48);		
	}

	/**
	 * 
	 * @param map
	 * @return	the uid of the module a map belongs to
	 */
	short getModuleUID(int map) {
		return (short) (map >>> 16);		
	}

	public long getFreeUID() {
		long uid = 256;
		while (entities.asMap().containsKey(uid)) {
			uid++;
		}
		return uid;
	}
	
	/**
	 * Stores all remaining entities in the cache in the temp folder on disk.
	 */
	public void flush() {
		entities.asMap().values().forEach(this::saveEntity);
	}
	
	/**
	 * Saves an entity in a json file.
	 * 
	 * @param entity
	 */
	private void saveEntity(Entity entity) {
		try {
			files.saveFile(gson.toJsonTree(entity), translator, "entities", entity.uid + ".json");
		} catch (IOException e) {
			logger.severe("could not save " + entity);
		}
	}

	private Entity loadEntity(long uid) throws IOException {
		JsonElement element = files.loadFile(translator, "entities", uid + ".json");
		return gson.fromJson(element, Entity.class);
	}
	
	public void addModule(Module module) {
		modules.add(module);
	}
	
	/**
	 * 
	 * @param module
	 * @return	the uid of the module
	 */
	public short getModuleUID(String module) {
		return uids.get(module);
	}
	
	/**
	 * Sets the uid of the given module. If another module with the same uid 
	 * was already present, this module is moved to the next free uid.
	 * 
	 * @param module
	 * @param uid
	 */
	public void setModuleUID(String module, short uid) {
		if (uids.containsValue(uid)) {
			String mod = uids.inverse().get(uid);
			short index = 0;
			while (uids.containsValue(++index));
			uids.put(mod, index);
		}
		
		uids.put(module, uid);
	}	
	
	private final class EntityListener implements RemovalListener<Long, Entity> {
		@Override
		public void onRemoval(RemovalNotification<Long, Entity> notification) {
			logger.finest(notification.getValue() + " removed from manager");
			saveEntity(notification.getValue());
		}		
	}
	
	private final class MapListener implements RemovalListener<String, Map> {
		@Override
		public void onRemoval(RemovalNotification<String, Map> notification) {
			logger.finest(notification.getValue() + " removed from manager");
		}		
	}
}
