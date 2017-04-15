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
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import neon.editor.dialogs.CreatureEditor;
import neon.editor.dialogs.SettingsEditor;
import neon.system.files.FileUtils;
import neon.system.files.NeonFileSystem;
import neon.system.logging.NeonLogFormatter;
import neon.system.resources.CreatureLoader;
import neon.system.resources.MissingLoaderException;
import neon.system.resources.MissingResourceException;
import neon.system.resources.ModuleLoader;
import neon.system.resources.RCreature;
import neon.system.resources.RModule;
import neon.system.resources.ResourceManager;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

/**
 * The neon roguelike editor.
 * 
 * @author mdriesen
 *
 */
public class Editor extends Application {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private MenuItem saveItem, settingsItem;
	@FXML private TreeView<String> creatureTree, itemTree;
	
	private final NeonFileSystem files = new NeonFileSystem();
	private final ResourceManager resources = new ResourceManager(files);
	private final EventBus bus = new EventBus("Editor Bus");
	private Stage stage;	
	private Scene scene;
	private RModule module;
	
	/**
	 * Initializes this {@code Editor}.
	 */
	public Editor() {
		bus.register(this);
		
		try {
			files.setTemporaryFolder(Paths.get("temp"));
		} catch (IOException e) {
			logger.severe("could not initialize file system");			
		}
		
		resources.addLoader("module", new ModuleLoader());
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("editor.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load editor ui");
		}
		
		settingsItem.setDisable(true);
		saveItem.setDisable(true);
	}
	
	@Override
	public void start(Stage stage) {
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
	
	@FXML private void editSettings(ActionEvent event) {
		new SettingsEditor(module, stage, bus).show();
	}
	
	@FXML private void showAbout(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("The Neon Roguelike Editor");
		alert.setContentText("Version 0.5.0 \n"
				+ "Copyright (C) 2017 - mdriesen");
		alert.showAndWait();
	}
	
	@FXML private void showOpen(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Open Module");
        chooser.setInitialDirectory(new File("data")); 
		File file = chooser.showDialog(stage);
		if (file != null && file.exists()) {
			loadModule(file);			
		}
	}
	
	@FXML private void showNew(ActionEvent event) {
		// ask for a new module id
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Create New Module");
		dialog.setHeaderText("Give an id for the new module.");
		dialog.setContentText("Module id:");
		Optional<String> result = dialog.showAndWait();
		
		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			Path path = Paths.get("data", id);
			
			// check if id didn't exist already
			if (Files.exists(path)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Module conflict");
				alert.setContentText("The module id already exists. Use another id.");
				alert.showAndWait();
				return;
			}
			
			// create the new module
			try {
				Files.createDirectories(path);
				files.addModule(id);				
				module = new RModule(id, "");
				resources.addResource(module);
				settingsItem.setDisable(false);
				stage.setTitle("The Neon Roguelike Editor - " + module.getID());
				saveItem.setDisable(false);
			} catch (IOException e) {
				logger.severe("could not create module directory " + path);
			} catch (MissingLoaderException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	@FXML private void quit(ActionEvent event) {
		System.exit(0);
	}
	
	private void loadModule(File file) {
		try {
			files.addModule(file.getName());
			module = resources.getResource(file.getName());
			settingsItem.setDisable(false);
			stage.setTitle("The Neon Roguelike Editor - " + module.getID());
			saveItem.setDisable(false);
			loadCreatures();
		} catch (FileNotFoundException e) {
			logger.info(e.getMessage());
		} catch (MissingResourceException e) {
			logger.warning(e.getMessage());
		} catch (MissingLoaderException e) {
			logger.warning(e.getMessage());
		}
	}
	
	private void loadCreatures() {
		TreeItem<String> root = new TreeItem<>();
		creatureTree.setRoot(root);
		creatureTree.setShowRoot(false);
		creatureTree.setOnMouseClicked(new CreatureTreeHandler());
		resources.addLoader("creature", new CreatureLoader());
		
		try {
			for (String creature : resources.listResources("creatures")) {
				TreeItem<String> item = new TreeItem<>(creature);
				root.getChildren().add(item);
			}
		} catch (IOException e) {
			logger.warning("could not load creature list");
		}
	}
	
	@FXML private void saveModule(ActionEvent event) {
		FileUtils.moveFolder(Paths.get("temp"), Paths.get("data", module.getID()));
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
		if (event.toString().equals("module")) {
			module = event.getResource();
			stage.setTitle("The Neon Roguelike Editor - " + module.getID());
			namespace = "global";
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
	
	private class CreatureTreeHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (event.getClickCount() == 2) {
	            TreeItem<String> item = creatureTree.getSelectionModel().getSelectedItem();
	            if (item != null) {
	            	RCreature creature;
					try {
						creature = resources.getResource("creatures", item.getValue());
		            	new CreatureEditor(creature, stage, bus).show();
					} catch (MissingResourceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MissingLoaderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        }
		}		
	}
}
