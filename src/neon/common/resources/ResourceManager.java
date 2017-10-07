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

package neon.common.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import com.google.common.collect.MapMaker;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.loaders.ResourceLoader;

/**
 * Manages all game resources. Resources are stored with weak references and 
 * (re)loaded on demand. Beware: changes to a resource should be explicitly
 * saved. Any changes will otherwise be lost if the resource is discarded and
 * later reloaded.
 * 
 * @author mdriesen
 *
 */
public class ResourceManager implements ResourceProvider {
	private final static Logger logger = Logger.getGlobal();
	
	private final NeonFileSystem files;
	// TODO: hier een guava table voor gebruiken?
	private final Map<String, Map<String, Resource>> resources = new HashMap<>();
	private final Map<String, ResourceLoader> loaders = new HashMap<>();
	
	/**
	 * Creates a resource manager that uses the given filesystem to load its
	 * resources.
	 * 
	 * @param files
	 */
	public ResourceManager(NeonFileSystem files) {
		this.files = files;
	}
	
	/**
	 * Adds a new resource to the manager. The resource is stored in the 
	 * temporary folder. If this resource already existed, it will be 
	 * overwritten without warning. 
	 * 
	 * @param namespace
	 * @param resource
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public <T extends Resource> void addResource(T resource) throws IOException {
		String namespace = resource.namespace;
		
		// create namespace if necessary
		if (!resources.containsKey(namespace)) {
			logger.info("creating namespace " + namespace);
			resources.put(namespace, new MapMaker().weakValues().makeMap());
		}
		
		// add resource to the weak value map
		resources.get(namespace).put(resource.id, resource);
		
		// save resource to temp folder
		String type = resource.type;
		if (loaders.containsKey(type)) {
			Document doc = new Document(loaders.get(type).save(resource));
			if (namespace.equals("global")) {
				files.saveFile(doc, new XMLTranslator(), resource.id + ".xml");			
			} else {
				files.saveFile(doc, new XMLTranslator(), namespace, resource.id + ".xml");							
			}
		} else {
			throw new IllegalStateException("Loader for resource type <" + resource.type + "> was not found.");
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
	@Override @SuppressWarnings("unchecked")
	public <T extends Resource> T getResource(String namespace, String id) throws ResourceException {
		// check if resource was already loaded
		if (resources.containsKey(namespace)) {
			if (resources.get(namespace).containsKey(id)) {
				return (T) resources.get(namespace).get(id);
			} 
		} else {
			// namespace did not exist yet, create it now
			logger.info("creating namespace " + namespace);
			resources.put(namespace, new MapMaker().weakValues().makeMap());
		}

		// resource was not loaded, do it now
		try {
			Element resource;
			if (namespace.equals("global")) {
				resource = files.loadFile(new XMLTranslator(), id + ".xml").getRootElement();				
			} else {
				resource = files.loadFile(new XMLTranslator(), namespace, id + ".xml").getRootElement();				
			}
			String type = resource.getName();
			if(loaders.containsKey(type)) {
				T value = (T) loaders.get(type).load(resource);
				resources.get(namespace).put(id, value);
				return value;
			} else {
				throw new IllegalStateException("Loader for resource type <" + resource.getName() + "> was not found.");
			}
		} catch (IOException e) {
			throw new ResourceException("Resource " + namespace + ":" + id + " was not found.", e);
		}
	}
	
	/**
	 * Adds a loader for the given resource type to the manager.
	 * 
	 * @param type
	 * @param loader
	 */
	public <T extends Resource> void addLoader(String type, ResourceLoader<T> loader) {
		loaders.put(type, loader);
	}
	
	/**
	 * List all resources of a given type.
	 * 
	 * @param namespace
	 * @return
	 */
	public Set<String> listResources(String namespace) {
		HashSet<String> set = new HashSet<>();
		
		for (String file : files.listFiles(namespace)) {
			set.add(file.replace(".xml", ""));
		}
		
		return set;
	}
	
	/**
	 * Checks whether the given resource is present on disk.
	 * 
	 * @param namespace
	 * @param id
	 * @return
	 */
	public boolean hasResource(String namespace, String id) {
		return files.listFiles(namespace).contains(id + ".xml");
	}
	
	/**
	 * Removes the given resource from the resource manager. 
	 * 
	 * @param namespace
	 * @param id
	 */
	public void removeResource(String namespace, String id) {
		if (resources.containsKey(namespace)) {
			resources.get(namespace).remove(id);
		}
		
		files.deleteFile(namespace, id + ".xml");
	}
}