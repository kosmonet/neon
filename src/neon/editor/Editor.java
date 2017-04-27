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
		ui = new UserInterface(resources, bus);
		
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
	 * Creates a new module.
	 * 
	 * @param event
	 * @throws IOException
	 * @throws MissingLoaderException
	 */
	@Subscribe
	private void createModule(LoadEvent.Create event) throws IOException, MissingLoaderException {
		Path path = event.getPath();
		id = path.getFileName().toString();

		// create the new module
		Files.createDirectories(path);
		// editing is done in temp, so don't add the actual module folder to the file system
		resources.addResource(new RModule(id, ""));
	}
	
	/**
	 * Loads an existing module for editing.
	 * 
	 * @param event
	 * @throws FileNotFoundException
	 */
	@Subscribe
	private void loadModule(LoadEvent.Load event) throws FileNotFoundException {
		File file = event.getFile();
		id = file.getName();
		// editing is done in temp, so don't add the actual module folder to the file system
		FileUtils.copyFolder(file.toPath(), Paths.get("temp"));
		logger.fine("loaded module " + id);
	}

	/**
	 * Saves the module that is currently being edited to disk.
	 * 
	 * @param event
	 */
	@Subscribe
	private void saveModule(SaveEvent.Module event) {
		FileUtils.clearFolder(Paths.get("data", id));
		FileUtils.copyFolder(Paths.get("temp"), Paths.get("data", id));
		logger.fine("saved module " + id);
	}
	
	/**
	 * Saves an edited resource to disk.
	 * 
	 * @param event
	 */
	@Subscribe
	private void saveResource(SaveEvent.Resources event) {	
		String namespace = event.getNamespace();
		
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
