/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.BiMap;
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
	private static final Logger LOGGER = Logger.getGlobal();
	private static final GsonBuilder BUILDER = new GsonBuilder()
			.registerTypeAdapter(Entity.class, new EntityAdapter());
	private static final Gson GSON = BUILDER.create();
	private static final JsonTranslator TRANSLATOR = new JsonTranslator();

	private final Cache<Long, Entity> entities = CacheBuilder.newBuilder().removalListener(new EntityListener()).softValues().build();
	private final HashMap<Class<?>, EntityBuilder> builders = new HashMap<>();
	private final NeonFileSystem files;
	private final Cache<String, Map> maps = CacheBuilder.newBuilder().removalListener(new MapListener()).softValues().build();
	private final BiMap<String, Short> uids = HashBiMap.create();
	private final Set<Module> modules = new HashSet<>();
	private final MapLoader loader;
	
	/**
	 * Initializes a new entity manager. The file system must not be null.
	 * 
	 * @param files	the server file system
	 * @param resources	the server resource manager
	 */
	public EntityManager(NeonFileSystem files, ResourceManager resources) {
		this.files = Objects.requireNonNull(files, "file system");
		loader = new MapLoader(files, resources, this);
	}
	
	/**
	 * Returns the entity with the given uid. If the entity is not present, an
	 * {@code IllegalArgumentException} is thrown.
	 * 
	 * @param uid	an entity uid
	 * @return	an {@code Entity} with the given uid
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
	 * @param uid	the uid of the entity to remove
	 */
	public void removeEntity(long uid) {
		entities.invalidate(uid);
	}
	
	/**
	 * Creates a new entity from the given resource and uid. The new entity
	 * is added to the cache.
	 * 
	 * @param uid	the uid of the new entity
	 * @param resource	the resource the entity is based on
	 * @return	a new {@code Entity}
	 */
	@SuppressWarnings("unchecked")
	public Entity createEntity(long uid, Resource resource) {
		Entity entity = builders.get(resource.getClass()).build(uid, resource);
		entities.put(uid, entity);		
		return entity;
	}
	
	/**
	 * Adds an entity builder. The type and the builder itself must not be null.
	 * 
	 * @param type	the type of object the builder will build
	 * @param builder	an {@code EntityBuilder}
	 */
	public <T extends Resource> void addBuilder(Class<T> type, EntityBuilder<? super T> builder) {
		builders.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(builder, "builder"));
	}
	
	/**
	 * Calculates the full map uid given the module name and the base uid
	 * of the map within the module.
	 * 
	 * @param base	the 16-bit base uid of a map
	 * @param module	the id of the module the map belongs to
	 * @return	the full 32-bit map uid
	 */
	public int getMapUID(short base, String module) {
		return ((int) getModuleUID(module) << 16) | base;
	}
	
	/**
	 * Returns the map with the given id.
	 * 
	 * @param id	the resource id of the map
	 * @return	a {@code Map}
	 * @throws IOException	if the map is missing
	 * @throws ResourceException	if the map can't be loaded
	 */
	public Map getMap(String id) throws IOException, ResourceException {
		// load the map if it didn't exist yet
		if (!maps.asMap().containsKey(id)) {
			maps.put(id,  loader.loadMap(id));
		}
		
		return maps.getIfPresent(id);
	}
	
	/**
	 * Return the 16-bit uid of the module the given entity belongs to.
	 * 
	 * @param entity	a full 64-bit entity uid
	 * @return	the uid of the module an entity belongs to
	 */
	short getModuleUID(long entity) {
		return (short) (entity >>> 48);		
	}

	/**
	 * Returns the 16-bit uid of the module the given map belongs to.
	 * 
	 * @param map	the full 32-bit uid of a map
	 * @return	the uid of the module the map belongs to
	 */
	short getModuleUID(int map) {
		return (short) (map >>> 16);		
	}

	/**
	 * Returns an entity uid that is still unused.
	 * 
	 * @return	a full 64-bit uid
	 */
	public long getFreeUID() {
		long uid = 256;
		while (entities.asMap().containsKey(uid)) {
			uid++;
		}
		return uid;
	}
	
	/**
	 * Saves all remaining entities in the entity cache to the temp folder 
	 * on disk.
	 */
	public void flushEntities() {
		entities.asMap().values().forEach(this::saveEntity);
	}
	
	/**
	 * Saves all remaining maps in the map cache to the temp folder on disk.
	 */
	public void flushMaps() {
		maps.asMap().values().forEach(this::saveMap);
	}
	
	/**
	 * Saves an entity in a json file.
	 * 
	 * @param entity	the {@code Entity} to save
	 */
	private void saveEntity(Entity entity) {
		try {
			files.saveFile(GSON.toJsonTree(entity), TRANSLATOR, "entities", entity.uid + ".json");
		} catch (IOException e) {
			LOGGER.severe("could not save " + entity);
		}
	}
	
	/**
	 * Saves a map to the temp folder on disk.
	 * 
	 * @param map	the {@code Map} to save
	 */
	private void saveMap(Map map) {
		loader.saveMap(map);
	}
	
	/**
	 * Loads an entity from a json file.
	 * 
	 * @param uid	the full 64-bit uid of the entity to load
	 * @return	an {@code Entity}
	 * @throws IOException	if the entity is missing
	 */
	private Entity loadEntity(long uid) throws IOException {
		JsonElement element = files.loadFile(TRANSLATOR, "entities", uid + ".json");
		return GSON.fromJson(element, Entity.class);
	}
	
	/**
	 * Adds a module to the game.
	 * 
	 * @param module	the {@code Module} to add
	 */
	public void addModule(Module module) {
		modules.add(Objects.requireNonNull(module, "module"));
	}
	
	/**
	 * returns the 16-bit uid of the module with the given name.
	 * 
	 * @param module	the name of the module
	 * @return	the uid of the module
	 */
	public short getModuleUID(String module) {
		return uids.get(module);
	}
	
	/**
	 * Sets the uid of the given module. If another module with the same uid 
	 * was already present, this module is moved to the next free uid.
	 * 
	 * @param module	a module id
	 * @param uid	the module uid
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
	
	/**
	 * A removal listener that handles the removal of entities from the entity
	 * cache.
	 * 
	 * @author mdriesen
	 *
	 */
	private final class EntityListener implements RemovalListener<Long, Entity> {
		@Override
		public void onRemoval(RemovalNotification<Long, Entity> notification) {
			LOGGER.finest(notification.getValue() + " removed from manager");
			saveEntity(notification.getValue());
		}		
	}
	
	/**
	 * A removal listener that handles the removal of maps from the map cache.
	 * 
	 * @author mdriesen
	 *
	 */
	private final class MapListener implements RemovalListener<String, Map> {
		@Override
		public void onRemoval(RemovalNotification<String, Map> notification) {
			LOGGER.finest(notification.getValue() + " removed from manager");
			saveMap(notification.getValue());
		}		
	}
}
