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

package neon.editor.ui;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import neon.common.files.NeonFileSystem;
import neon.common.resources.ResourceManager;
import neon.editor.LoadEvent;
import neon.editor.controllers.CreatureHandler;
import neon.editor.controllers.ItemHandler;
import neon.editor.controllers.MapHandler;
import neon.editor.controllers.MenuHandler;
import neon.editor.controllers.TerrainHandler;
import neon.editor.resource.CEditor;

/**
 * The {@code UserInterface} takes care of most ui-related editor functionality.
 * 
 * @author mdriesen
 *
 */
public final class UserInterface {
	private static final Logger logger = Logger.getGlobal();
	
	private final CreatureHandler creatureHandler;
	private final MapHandler mapHandler;
	private final MenuHandler menuHandler;
	private final ItemHandler itemHandler;
	private final TerrainHandler terrainHandler;
	
	private Stage stage;
	private Scene scene;
	
	/**
	 * Initializes the {@code UserInterface}.
	 * 
	 * @param files
	 * @param resources
	 * @param bus
	 * @param config
	 */
	public UserInterface(NeonFileSystem files, ResourceManager resources, EventBus bus, CEditor config) {
		// separate handlers for all the different ui elements
		menuHandler = new MenuHandler(resources, bus, this);
		bus.register(menuHandler);
		mapHandler = new MapHandler(resources, bus, config);
		bus.register(mapHandler);
		creatureHandler = new CreatureHandler(resources, bus);
		bus.register(creatureHandler);
		itemHandler = new ItemHandler(resources, bus);
		bus.register(itemHandler);
		terrainHandler = new TerrainHandler(resources, bus);
		bus.register(terrainHandler);
		
		// load the user interface
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
		loader.setControllerFactory(this::getController);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("editor.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load editor ui: " + e.getMessage());
		}		
	}
	
	/**
	 * Returns the correct controller for a JavaFX node.
	 * 
	 * @param type
	 * @return
	 */
	private Object getController(Class<?> type) {
		if(type.equals(MenuHandler.class)) {
			return menuHandler;	
		} else if (type.equals(MapHandler.class)) {
			return mapHandler;
		} else if (type.equals(CreatureHandler.class)) {
			return creatureHandler;
		} else if (type.equals(ItemHandler.class)) {
			return itemHandler;
		} else if (type.equals(TerrainHandler.class)) {
			return terrainHandler;
		} else {
			throw new IllegalArgumentException("No controller found for class " + type + "!");
		}
	}
	
	/**
	 * 
	 * @return the main window of the editor
	 */
	public Window getWindow() {
		return stage;
	}
	
	/**
	 * Shows the main window on screen.
	 * 
	 * @param stage
	 */
	public void start(Stage stage) {
		this.stage = stage;
		stage.setTitle("The Neon Roguelike Editor");
		stage.setScene(scene);
		stage.setWidth(1440);
		stage.setMinWidth(800);
		stage.setHeight(720);
		stage.setMinHeight(600);
		stage.show();
		stage.centerOnScreen();
		stage.setOnCloseRequest(event -> System.exit(0));
	}

	/**
	 * Sets the title of the main window.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onModuleLoad(LoadEvent event) {
		stage.setTitle("The Neon Roguelike Editor - " + event.id);
	}
	
	/**
	 * Checks if any resources are still opened and should be saved. This 
	 * method should be called when saving a module or exiting the editor.
	 */
	public void saveResources() {
		// check if any maps are still opened
		mapHandler.saveMaps();
	}
}
