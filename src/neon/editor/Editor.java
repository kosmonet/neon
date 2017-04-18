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

package neon.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.stage.Stage;
import neon.system.files.FileUtils;
import neon.system.files.NeonFileSystem;
import neon.system.logging.NeonLogFormatter;
import neon.system.resources.MissingLoaderException;
import neon.system.resources.ModuleLoader;
import neon.system.resources.RModule;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

/**
 * The neon roguelike editor.
 * 
 * @author mdriesen
 *
 */
public class Editor extends Application {
	private static final Logger logger = Logger.getGlobal();
	
	private final NeonFileSystem files = new NeonFileSystem();
	private final ResourceManager resources = new ResourceManager(files);
	private final EventBus bus = new EventBus("Editor Bus");
	private final UserInterface ui;	// where all JavaFX business takes place
	private String id;
	
	/**
	 * Initializes this {@code Editor}.
	 */
	public Editor() {
		bus.register(this);
		ui = new UserInterface(this, bus);
		
		try {
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			logger.severe("could not initialize file system");			
		}
		
		resources.addLoader("module", new ModuleLoader());
	}
	
	@Override
	public void start(Stage stage) {
		ui.start(stage);
	}
	
	/**
	 * 
	 * @return the id of the currently loaded module
	 */
	String getModuleID() {
		return id;
	}
	
	/**
	 * 
	 * @return the {@code ResourceManager}
	 */
	ResourceManager getResources() {
		return resources;
	}
	
	/**
	 * Creates a new module.
	 * 
	 * @param path
	 * @throws IOException
	 * @throws MissingLoaderException
	 */
	void createModule(Path path) throws IOException, MissingLoaderException {
		id = path.getFileName().toString();

		// create the new module
		Files.createDirectories(path);
		// editing is done in temp, so don't add the actual module folder to the file system
		resources.addResource(new RModule(id, ""));
	}
	
	/**
	 * Loads an existing module for editing.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 */
	void loadModule(File file) throws FileNotFoundException {
		id = file.getName();
		// editing is done in temp, so don't add the actual module folder to the file system
		FileUtils.copyFolder(file.toPath(), Paths.get("temp"));
	}

	/**
	 * Save the module that is currently being edited to disk.
	 */
	void saveModule() {
		FileUtils.clearFolder(Paths.get("data", id));
		FileUtils.copyFolder(Paths.get("temp"), Paths.get("data", id));
		try {
			bus.post(new SaveEvent("module", resources.getResource(id)));
		} catch (ResourceException e) {
			logger.severe(e.getMessage());
		}
	}
	
	/**
	 * Saves an edited resource to disk.
	 * 
	 * @param event
	 */
	@Subscribe
	public void saveResource(SaveEvent event) {	
		String namespace = event.toString();

		// special consideration if this concerns the module resource
		if (event.toString().equals("settings")) {
			namespace = "global";
		} else if (namespace.equals("module")) {
			return;
		}
		
		try {
			resources.addResource(namespace, event.getResource());
			logger.fine("saved resource " + event.getResource().getID());
		} catch (MissingLoaderException e) {
			logger.severe(e.getMessage());
		} catch (IOException e) {
			logger.severe("failed to save resource " + event.getResource().getID());
		}
	}
	
	public static void main(String[] args) {
		// set up logging
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		handler.setFormatter(new NeonLogFormatter());
		logger.addHandler(handler);
		
		launch(args);
	}
}
