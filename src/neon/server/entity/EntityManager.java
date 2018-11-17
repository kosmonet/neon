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
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.jdom2.Document;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashBiMap;

import neon.common.entity.Entity;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;

/**
 * This class manages all loaded entities in the current game (and by extension
 * all loaded modules and maps). 
 * 
 * @author mdriesen
 *
 */
public final class EntityManager {
	private static final Logger logger = Logger.getGlobal();

	private final Cache<Long, Entity> entities = CacheBuilder.newBuilder().removalListener(new EntityListener()).softValues().build();
	private final HashMap<Class<?>, EntityBuilder> builders = new HashMap<>();
	private final EntitySaver saver;
	private final NeonFileSystem files;
	private final Cache<Integer, Map> maps = CacheBuilder.newBuilder().removalListener(new MapListener()).softValues().build();
	private final HashBiMap<String, Short> uids = HashBiMap.create();

	public EntityManager(NeonFileSystem files, EntitySaver saver) {
		this.files = files;
		this.saver = saver;	
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
	 * Calculates the full map uid given the module name and the base uid
	 * of the map within the module.
	 * 
	 * @param base
	 * @param module
	 * @return
	 */
	public int getMapUID(short base, String module) {
		return ((int)getModuleUID(module) << 16) | base;
	}
	
	public Map getMap(Integer uid) {
		return maps.getIfPresent(uid);
	}
	
	public void addMap(Map map) {
		maps.put(map.getUid(), map);
	}
	
	public <T extends Resource> void addBuilder(Class<T> type, EntityBuilder<? super T> builder) {
		builders.put(type, builder);
	}
	
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
	
	public Collection<Entity> getEntities() {
		return entities.asMap().values();
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
	 * Stores all remaining entities in the cache in the temp folder on disk.
	 */
	public void flush() {
		entities.asMap().values().forEach(entity -> saveEntity(entity));
	}
	
	public long getFreeUID() {
		long uid = 256;
		while (entities.asMap().containsKey(uid)) {
			uid++;
		}
		return uid;
	}
	
	private void saveEntity(Entity entity) {
		Document doc = new Document(saver.save(entity));
		try {
			files.saveFile(doc, new XMLTranslator(), "entities", Long.toString(entity.uid) + ".xml");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private Entity loadEntity(long uid) throws IOException, ResourceException {
		Document doc = files.loadFile(new XMLTranslator(), "entities", Long.toString(uid) + ".xml");
		return saver.load(uid, doc.getRootElement());
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
	
	private final class MapListener implements RemovalListener<Integer, Map> {
		@Override
		public void onRemoval(RemovalNotification<Integer, Map> notification) {
			logger.finest(notification.getValue() + " removed from manager");
		}		
	}
}
