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

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import neon.system.resources.ResourceManager;

/**
 * The {@code UserInterface} takes care of most ui-related editor functionality.
 * 
 * @author mdriesen
 *
 */
public class UserInterface {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private TreeView<Card> itemTree;
	@FXML private TabPane tabs;
	
	private final CreatureHandler creatureHandler;
	private final MapHandler mapHandler;
	private final MenuHandler menuHandler;
	private Stage stage;	
	private Scene scene;
	
	/**
	 * Initializes the {@code UserInterface}.
	 * 
	 * @param editor
	 * @param bus
	 */
	UserInterface(ResourceManager resources, EventBus bus) {
		// separate handlers for all the different ui elements
		menuHandler = new MenuHandler(resources, bus);
		bus.register(menuHandler);
		mapHandler = new MapHandler(resources);
		bus.register(mapHandler);
		creatureHandler = new CreatureHandler(resources, bus);
		bus.register(creatureHandler);
		
		// load the user interface
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ui/Editor.fxml"));
		loader.setControllerFactory(type -> getController(type));
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("ui/editor.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("failed to load editor ui");
		}		
	}
	
	private Object getController(Class<?> type) {
		if(type.equals(MenuHandler.class)) {
			return menuHandler;	
		} else if (type.equals(MapHandler.class)) {
			return mapHandler;
		} else if (type.equals(CreatureHandler.class)) {
			return creatureHandler;
		} else {
			throw new IllegalArgumentException("No controller found for class " + type + "!");
		}
	}
	
	/**
	 * Shows the main window on screen.
	 * 
	 * @param stage
	 */
	void start(Stage stage) {
		this.stage = stage;
		stage.setTitle("The Neon Roguelike Editor");
		stage.setScene(scene);
		stage.setWidth(1280);
		stage.setMinWidth(800);
		stage.setHeight(720);
		stage.setMinHeight(600);
		stage.show();
		stage.centerOnScreen();
	}

	@Subscribe
	private void loadModule(LoadEvent event) {
		stage.setTitle("The Neon Roguelike Editor - " + event.getModuleID());
	}
}
