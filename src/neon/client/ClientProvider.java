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

import neon.system.resources.Resource;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceProvider;

/**
 * Manages resources for the client.
 * 
 * @author mdriesen
 *
 */
public class ClientProvider implements ResourceProvider {
	private final Map<String, Map<String, Resource>> resources = new HashMap<>();

	/**
	 * Adds a collection of resources to this provider.
	 * 
	 * @param collection
	 */
	public void addAll(Collection<Resource> collection) {
		for (Resource resource : collection) {
			// make sure namespace exists
			if(!resources.containsKey(resource.getNamespace())) {
				resources.put(resource.getNamespace(), new HashMap<>());
			}
			
			// add resource to correct namespace
			resources.get(resource.getNamespace()).put(resource.getID(), resource);
		}
	}
	
	public void clear() {
		resources.clear();
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
}
