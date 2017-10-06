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

package neon.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceProvider;
import neon.entity.EntityProvider;
import neon.entity.entities.Entity;

/**
 * Manages resources for the client.
 * 
 * @author mdriesen
 *
 */
public class ClientProvider implements ResourceProvider, EntityProvider {
	private final Map<String, Map<String, Resource>> resources = new HashMap<>();
	private final Map<Long, Entity> entities = new HashMap<>();

	/**
	 * Clears all entities and resources from this provider.
	 */
	public void clear() {
		resources.clear();
		entities.clear();
	}
	
	/**
	 * Adds a collection of resources to this provider.
	 * 
	 * @param collection
	 */
	public void addResources(Collection<Resource> collection) {
		for (Resource resource : collection) {
			// make sure namespace exists
			if (!resources.containsKey(resource.namespace)) {
				resources.put(resource.namespace, new HashMap<>());
			}
			
			// add resource to correct namespace
			resources.get(resource.namespace).put(resource.id, resource);
		}
	}
	
	@Override @SuppressWarnings("unchecked")
	public <T extends Resource> T getResource(String namespace, String id) throws ResourceException {
		// check if resource was already loaded
		if (resources.containsKey(namespace) && resources.get(namespace).containsKey(id)) {
			return (T) resources.get(namespace).get(id);
		} else {
			throw new ResourceException("Resource " + namespace + ":" + id + " was not found.");
		}
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
	
	@Override @SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(long uid) {
		return (T) entities.get(uid);
	}

	@Override
	public Collection<Entity> getEntities() {
		return entities.values();
	}
}
