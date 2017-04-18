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
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import neon.editor.dialogs.SettingsEditor;
import neon.system.resources.MissingLoaderException;

/**
 * The {@code UserInterface} takes care of most ui-related editor functionality.
 * 
 * @author mdriesen
 *
 */
public class UserInterface {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private MenuItem saveItem, settingsItem, openItem, newItem;
	@FXML private TreeView<Card> creatureTree, itemTree;
	
	private final Editor editor;
	private final EventBus bus;
	private final CreatureHandler creatureHandler;
	private Stage stage;	
	private Scene scene;
	
	/**
	 * Initializes the {@code UserInterface}.
	 * 
	 * @param editor
	 * @param bus
	 */
	UserInterface(Editor editor, EventBus bus) {
		this.editor = editor;
		this.bus = bus;
		
		// load the user interface
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

		// listeners for ui elements that represent resources
		creatureHandler = new CreatureHandler(creatureTree, editor.getResources(), bus);
		bus.register(creatureHandler);
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
			try {
				editor.loadModule(file);
				settingsItem.setDisable(false);
				saveItem.setDisable(false);
				openItem.setDisable(true);
				newItem.setDisable(true);
				stage.setTitle("The Neon Roguelike Editor - " + file.getName());
				creatureHandler.loadCreatures();
			} catch (FileNotFoundException e) {
				logger.severe("could not open module " + file.getName());
			}
		}
	}

	@FXML private void editSettings(ActionEvent event) {
		String id = editor.getModuleID();
		new SettingsEditor(editor.getResources(), id, bus).show(stage);

	}
	
	@FXML private void quit(ActionEvent event) {
		System.exit(0);
	}
	
	@FXML private void saveModule(ActionEvent event) {
		editor.saveModule();
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
				alert.setContentText("The module id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the new module
			try {
				editor.createModule(path);
				stage.setTitle("The Neon Roguelike Editor - " + id);
				settingsItem.setDisable(false);
				saveItem.setDisable(false);
				openItem.setDisable(true);
				newItem.setDisable(true);
				creatureHandler.loadCreatures();
			} catch (MissingLoaderException e) {
				logger.severe(e.getMessage());
			} catch (IOException e) {
				logger.severe("could not create module " + id);
			}
		}
	}
}
