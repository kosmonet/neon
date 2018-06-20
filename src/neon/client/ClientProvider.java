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

package neon.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import neon.entity.EntityProvider;
import neon.entity.entities.Entity;

/**
 * Manages resources for the client.
 * 
 * @author mdriesen
 *
 */
public class ClientProvider implements EntityProvider {
	private final Map<Long, Entity> entities = new HashMap<>();

	/**
	 * Clears all entities and resources from this provider.
	 */
	public void clear() {
		entities.clear();
	}
	
	/**
	 * Adds a collection of entities to this provider.
	 * 
	 * @param collection
	 */
	public void addEntities(Collection<Entity> collection) {
		for (Entity entity : collection) {
			entities.put(entity.uid, entity);
		}
	}
	
	/**
	 * Adds an entity to this provider.
	 * 
	 * @param entity
	 */
	public void addEntity(Entity entity) {
		entities.put(entity.uid, entity);
	}
	
	@Override @SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(long uid) {
		return (T) entities.get(uid);
	}

	@Override
	public Collection<Entity> getEntities() {
		return entities.values();
	}
	
	/**
	 * Returns a list of entities with the given uid's.
	 * 
	 * @param uids
	 * @return
	 */
	public Collection<Entity> getEntities(Collection<Long> uids) {
		HashSet<Entity> set = new HashSet<>();
		
		for (long uid : uids) {
			set.add(entities.get(uid));
		}
		
		return set;
	}
	
	/**
	 * Checks whether an entity with the given uid already exists.
	 * 
	 * @param uid
	 * @return
	 */
	public boolean hasEntity(long uid) {
		return entities.containsKey(uid);
	}
}
