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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Element;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.stage.Stage;
import neon.common.files.FileUtils;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.logging.NeonLogFormatter;
import neon.common.resources.RModule;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ModuleLoader;
import neon.editor.resource.CEditor;
import neon.editor.ui.UserInterface;

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
	private final CEditor config = new CEditor();
	
	/**
	 * Initializes this {@code Editor}.
	 */
	public Editor() {
		bus.register(this);
		ui = new UserInterface(resources, bus, config);
		
		try {
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			logger.severe("could not set the temporary folder");			
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
	private void createModule(LoadEvent.Create event) throws IOException {
		Path path = event.getPath();
		String id = path.getFileName().toString();

		// create the new module
		Files.createDirectories(path);
		// editing is done in temp, so don't add the actual module folder to the file system
		RModule module = new RModule(id, id, "", -1, -1);
		resources.addResource(module);
		config.setActiveModule(module);
	}
	
	/**
	 * Loads an existing module for editing.
	 * 
	 * @param event
	 * @throws FileNotFoundException
	 * @throws ResourceException 
	 */
	@Subscribe
	private void loadModule(LoadEvent.Load event) throws FileNotFoundException, ResourceException {
		// set the active module id
		File file = event.getFile();
		String module = file.getName();
		
		// add the parent modules to the file system
		Path path = Paths.get(file.getPath(), module + ".xml");
		try (InputStream in = Files.newInputStream(path)) {
			Element root = new XMLTranslator().translate(in).getRootElement();
			for (Element parent : root.getChild("parents").getChildren()) {
				files.addModule(parent.getText());
			}
		} catch (IOException e) {
			throw new FileNotFoundException("module <" + module + "> not found");
		}
		
		// keep track of all resources defined in parent modules
		Multimap<String, Card> cards = event.getCards();
		for (String id : resources.listResources("creatures")) {
			Card card = new Card("creatures", id, resources, true);
			cards.put("creatures", card);
		}
		for (String id : resources.listResources("items")) {
			Card card = new Card("items", id, resources, true);
			cards.put("items", card);
		}
		for (String id : resources.listResources("maps")) {
			Card card = new Card("maps", id, resources, true);
			cards.put("maps", card);
		}
		for (String id : resources.listResources("terrain")) {
			Card card = new Card("terrain", id, resources, true);
			cards.put("terrain", card);
		}
		
		// editing is done in temp, so don't add the actual module folder to the file system
		FileUtils.copyFolder(file.toPath(), Paths.get("temp"));
		config.setActiveModule(resources.getResource(module));
		
		// keep track of all resources (re)defined in the active module
		for (String id : FileUtils.listFiles(Paths.get("temp", "creatures"))) {
			Card card = new Card("creatures", id.replaceAll(".xml", ""), resources, false);
			if (cards.get("creatures").contains(card)) {
				cards.remove("creatures", card);
				card = new Card("creatures", id.replaceAll(".xml", ""), resources, true);
				card.setRedefined(true);
			}
			cards.put("creatures", card);
		}
		for (String id : FileUtils.listFiles(Paths.get("temp", "items"))) {
			Card card = new Card("items", id.replaceAll(".xml", ""), resources, false);
			if (cards.get("items").contains(card)) {
				cards.remove("items", card);
				card = new Card("items", id.replaceAll(".xml", ""), resources, true);
				card.setRedefined(true);
			}
			cards.put("items", card);
		}
		for (String id : FileUtils.listFiles(Paths.get("temp", "terrain"))) {
			Card card = new Card("terrain", id.replaceAll(".xml", ""), resources, false);
			if (cards.get("terrain").contains(card)) {
				cards.remove("terrain", card);
				card = new Card("terrain", id.replaceAll(".xml", ""), resources, true);
				card.setRedefined(true);
			}
			cards.put("terrain", card);
		}
		for (String id : FileUtils.listFiles(Paths.get("temp", "maps"))) {
			Card card = new Card("maps", id.replaceAll(".xml", ""), resources, false);
			if (cards.get("maps").contains(card)) {
				cards.remove("maps", card);
				card = new Card("maps", id.replaceAll(".xml", ""), resources, true);
				card.setRedefined(true);
			}
			cards.put("maps", card);
		}
				
		logger.fine("loaded module " + module);
	}

	/**
	 * Saves the module that is currently being edited to disk.
	 * 
	 * @param event
	 */
	@Subscribe
	private void saveModule(SaveEvent.Module event) {
		String id = config.getActiveModule().id;
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
		try {
			resources.addResource(event.getResource());
			logger.fine("saved resource " + event.getResource().id);
		} catch (IOException e) {
			logger.severe("failed to save resource " + event.getResource().id);
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
