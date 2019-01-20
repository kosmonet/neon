/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018-2019 - Maarten Driesen
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

package neon.client.handlers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

import org.jdom2.Element;

import com.google.common.eventbus.Subscribe;

import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.Map;
import neon.common.entity.components.Shape;
import neon.common.event.ConfigurationEvent;
import neon.common.event.LoadEvent;
import neon.common.event.UpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

/**
 * Handler for general loading and saving related events.
 * 
 * @author mdriesen
 */
public class FileHandler {
	private static final Logger LOGGER = Logger.getGlobal();
	private static final XMLTranslator TRANSLATOR = new XMLTranslator();
	
	private final ResourceManager resources;
	private final ComponentManager components;
	private final Configuration config;
	private final NeonFileSystem files;
	
	/**
	 * The file system, component manager, resource manager and configuration 
	 * must not be null.
	 * 
	 * @param files	the file system used by the client
	 * @param components	the client component manager
	 * @param resources	the client resource manager
	 * @param config	the client configuration data
	 */
	public FileHandler(NeonFileSystem files, ComponentManager components, ResourceManager resources, Configuration config) {
		this.files = Objects.requireNonNull(files, "file system");
		this.components = Objects.requireNonNull(components, "component manager");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		this.config = Objects.requireNonNull(config, "configuration");
	}
	
	/**
	 * Configures the file system with the required modules.
	 * 
	 * @param event	the {@code ConfigurationEvent} describing the modules
	 * @throws FileNotFoundException	if a module is missing
	 */
	@Subscribe
	private void configure(ConfigurationEvent event) throws FileNotFoundException {
		for (String module : event.getModules()) {
			files.addModule(module);
		}
	}
	
	/**
	 * Handles the loading of a saved game.
	 * 
	 * @param event	the {@code LoadEvent} describing the saved game
	 * @throws NotDirectoryException	if the saved game folder is broken
	 */
	@Subscribe 
	private void onGameLoad(LoadEvent.Start event) throws NotDirectoryException {
		files.setSaveFolder(Paths.get("saves", event.save));
	}
	
	/**
	 * Handles a change of maps.
	 * 
	 * @param event	an {@code UpdateEvent} describing the change
	 * @throws ResourceException	if the map resource can't be loaded
	 * @throws IOException	if the map is missing
	 */
	@Subscribe
	private void onMapChange(UpdateEvent.Map event) throws ResourceException, IOException {
		// TODO: map xml file wordt tweemaal na mekaar ingeladen
		RMap resource = resources.getResource("maps", event.id);
		Element root;
		
		// check if the maps was saved in cache
		if (files.listFiles("maps").contains(Integer.toString(event.uid) + ".xml")) {
			// load the map from cache
			LOGGER.fine("loading map <" + event.uid + "> from temp folder");
			root = files.loadFile(TRANSLATOR, "maps", Integer.toString(event.uid) + ".xml").getRootElement();
		} else {
			// load the map from module
			LOGGER.fine("loading map <" + event.id + "> from module <" + resource.module + ">");
			root = files.loadFile(TRANSLATOR, "maps", event.id + ".xml").getRootElement();
		}
		
		Map map = new Map(resource, root);
		LOGGER.finest("moving player to map " + map.getId());
		Shape shape = components.getComponent(Configuration.PLAYER_UID, Shape.class);
		LOGGER.finest("moving player to position (" + shape.getX() + ", " + shape.getY() + ")");
		map.addEntity(Configuration.PLAYER_UID, shape.getX(), shape.getY());
		config.setCurrentMap(map);
	}
}
