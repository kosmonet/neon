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
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import neon.editor.dialogs.SettingsEditor;
import neon.system.files.NeonFileSystem;
import neon.system.logging.NeonLogFormatter;
import neon.system.resources.MissingLoaderException;
import neon.system.resources.MissingResourceException;
import neon.system.resources.ModuleLoader;
import neon.system.resources.RModule;
import neon.system.resources.ResourceManager;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

public class Editor extends Application {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private MenuItem settingsItem;
	
	private final NeonFileSystem files = new NeonFileSystem();
	private final ResourceManager resources = new ResourceManager(files);
	private Stage stage;	
	private Scene scene;
	private RModule module;
	
	public Editor() {
		try {
			files.setTemporaryFolder(Paths.get(".", "temp"));
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
		new SettingsEditor(module, stage).show();
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
		if (file.exists()) {
			loadModule(file);			
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
		} catch (FileNotFoundException e) {
			logger.info(e.getMessage());
		} catch (MissingResourceException e) {
			logger.warning(e.getMessage());
		} catch (MissingLoaderException e) {
			logger.warning(e.getMessage());
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
