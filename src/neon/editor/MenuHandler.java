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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import neon.editor.dialogs.SettingsEditor;
import neon.system.resources.ResourceManager;

public class MenuHandler {
	@FXML private MenuItem saveItem, settingsItem, openItem, newItem;
	@FXML private TabPane tabs;
	
	private final EventBus bus;
	private final ResourceManager resources;
	private String id;
	
	public MenuHandler(ResourceManager resources, EventBus bus) {
		this.resources = resources;
		this.bus = bus;
	}
	
	@FXML public void initialize() {
		settingsItem.setDisable(true);
		saveItem.setDisable(true);
	}

	@FXML private void showAbout(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(tabs.getScene().getWindow());
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
		File file = chooser.showDialog(tabs.getScene().getWindow());
		if (file != null && file.exists()) {
			bus.post(new LoadEvent.Load(file)); 
			loadModule(file.getName());
		}
	}

	@FXML private void showNew(ActionEvent event) {
		// ask for a new module id
		TextInputDialog dialog = new TextInputDialog();
		dialog.initOwner(tabs.getScene().getWindow());
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
				alert.initOwner(tabs.getScene().getWindow());
				alert.setTitle("Warning");
				alert.setHeaderText("Module conflict");
				alert.setContentText("The module id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the new module
			bus.post(new LoadEvent.Create(path));
			loadModule(id);
		}
	}
	
	@FXML private void quit(ActionEvent event) {
		System.exit(0);
	}
	
	@FXML private void saveModule(ActionEvent event) {
		bus.post(new SaveEvent.Module());
	}
	
	@FXML private void editSettings(ActionEvent event) {
		new SettingsEditor(resources, id, bus).show(tabs.getScene().getWindow());
	}
	
	private void loadModule(String id) {
		this.id = id;
		settingsItem.setDisable(false);
		saveItem.setDisable(false);
		openItem.setDisable(true);
		newItem.setDisable(true);
	}
}
