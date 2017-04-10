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

package neon.system.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import com.google.common.collect.MapMaker;
import neon.system.files.NeonFileSystem;
import neon.system.files.XMLTranslator;

/**
 * Manages all game resources. Resources are stored with weak references and 
 * (re)loaded on demand. Beware: changes to a resource should be explicitly
 * saved. Any changes will otherwise be lost if the resource is discarded and
 * later reloaded.
 * 
 * @author mdriesen
 *
 */
public class ResourceManager {
	private final static Logger logger = Logger.getGlobal();
	
	private final NeonFileSystem files;
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
	 * @throws MissingLoaderException 
	 * @throws IOException 
	 */
	public void addResource(String namespace, Resource resource) throws MissingLoaderException, IOException {
		// create namespace if necessary
		if (!resources.containsKey(namespace)) {
			logger.info("creating namespace " + namespace);
			resources.put(namespace, new MapMaker().weakValues().makeMap());
		}
		
		// add resource to the weak value map
		resources.get(namespace).put(resource.getID(), resource);
		
		// save resource to temp folder
		String type = resource.getType();
		if(loaders.containsKey(type)) {
			Document doc = new Document(loaders.get(type).save(resource));
			files.saveFile(doc, new XMLTranslator(), namespace, resource.getID() + ".xml");			
		} else {
			throw new MissingLoaderException("Loader for resource type <" + resource.getType() + "> was not found.");
		}
	}
	
	/**
	 * Returns a resource from a specific namespace.
	 * 
	 * @param namespace
	 * @param id
	 * @return the requested resource
	 * @throws MissingResourceException 
	 * @throws MissingLoaderException 
	 */
	@SuppressWarnings("unchecked")
	public <T extends Resource> T getResource(String namespace, String id) throws MissingResourceException, MissingLoaderException {
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
			Element resource = files.loadFile(new XMLTranslator(), namespace, id + ".xml").getRootElement();
			String type = resource.getName();
			if(loaders.containsKey(type)) {
				return (T) loaders.get(type).load(resource);
			} else {
				throw new MissingLoaderException("Loader for resource type <" + resource.getName() + "> was not found.");
			}
		} catch (IOException e) {
			throw new MissingResourceException("Resource " + namespace + ":" + id + " was not found.");
		}
	}
	
	/**
	 * Adds a loader for the given resource type to the manager.
	 * 
	 * @param type
	 * @param loader
	 */
	public void addLoader(String type, ResourceLoader loader) {
		loaders.put(type, loader);
	}
}
