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

package neon.common.resources;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import neon.common.resources.loaders.ResourceLoader;

/**
 * Manages all game resources. Resources are stored with soft references and 
 * (re)loaded on demand. Beware: changes to a resource should be explicitly
 * saved. Any changes will otherwise be lost if the resource is discarded 
 * and later reloaded.
 * 
 * @author mdriesen
 *
 */
public final class ResourceManager {
	private static final Logger logger = Logger.getGlobal();
	
	private final Table<String, String, SoftReference<Resource>> resources = HashBasedTable.create();
	private final Map<String, ResourceLoader> loaders = new HashMap<>();
	
	/**
	 * Adds a new resource to the manager. The resource is stored in the 
	 * temporary folder. If this resource already existed, it will be 
	 * overwritten without warning. 
	 * 
	 * @param namespace
	 * @param resource
	 * @throws IOException 
	 */
	public void addResource(Resource resource) throws IOException {
		String namespace = resource.namespace;
		
		// add resource to the table
		resources.put(namespace, resource.id, new SoftReference<Resource>(resource));
		
		// save resource to temp folder
		if (loaders.containsKey(namespace)) {
			loaders.get(namespace).save(resource);
		} else {
			throw new IllegalStateException("Loader for namespace <" + resource.namespace + "> was not found.");
		}		
	}
	
	/**
	 * Returns a resource from the global namespace. 
	 * 
	 * @param id
	 * @return
	 * @throws ResourceException
	 */
	public <T extends Resource> T getResource(String id) throws ResourceException {
		return getResource("global", id);	
	}
	
	/**
	 * Returns a resource from a specific namespace.
	 * 
	 * @param namespace
	 * @param id
	 * @return the requested resource
	 * @throws ResourceException 
	 */
	@SuppressWarnings("unchecked")
	public <T extends Resource> T getResource(String namespace, String id) throws ResourceException {
		// check if resource was already loaded
		if (resources.contains(namespace, id)) {
			if (resources.get(namespace, id).get() != null) {
				return (T) resources.get(namespace, id).get();
			} else {
				logger.finest("resource <" + namespace + ":" + id + "> was evicted from cache, reloading");
			}
		} 

		// resource was not loaded, do it now
		if (loaders.containsKey(namespace)) {
			try {
				T resource = (T) loaders.get(namespace).load(id);
				resources.put(namespace, id, new SoftReference<Resource>(resource));
				return resource;
			} catch (IOException e) {
				throw new ResourceException("Resource <" + namespace + ":" + id + "> was not found.", e);
			}
		} else {
			throw new IllegalStateException("Loader for namespace <" + namespace + "> was not found.");			
		}
	}

	/**
	 * Adds a loader for the given namespace.
	 * 
	 * @param type
	 * @param loader
	 */
	public void addLoader(ResourceLoader loader) {
		loaders.put(loader.getNamespace(), loader);
	}
	
	/**
	 * List all resources in a namespace.
	 * 
	 * @param namespace
	 * @return
	 */
	public Set<String> listResources(String namespace) {
		if (loaders.containsKey(namespace)) {
			return loaders.get(namespace).listResources();
		} else {
			throw new IllegalStateException("Loader for namespace <" + namespace + "> was not found.");			
		}
	}
	
	/**
	 * Checks whether the given resource is present.
	 * 
	 * @param namespace
	 * @param id
	 * @return
	 */
	public boolean hasResource(String namespace, String id) {
		if (loaders.containsKey(namespace)) {
			return loaders.get(namespace).listResources().contains(id);
		} else {
			return false;
		}
	}
	
	/**
	 * Removes the given resource from the resource manager (and the temporary
	 * folder). 
	 * 
	 * @param namespace
	 * @param id
	 */
	public void removeResource(String namespace, String id) {
		try {
			resources.remove(namespace,  id);
			if (loaders.containsKey(namespace)) {
				loaders.get(namespace).removeResource(id);
			}
		} catch (IOException e) {
			logger.finer("could not remove resource <" + namespace + ":" + id + ">"); 
		}
	}
}
