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

package neon.editor.dialogs;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import neon.editor.SaveEvent;
import neon.system.resources.RModule;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

/**
 * This resource editor shows a modal dialog window to edit the properties
 * of a module:
 * <ul>
 * 	<li>the title of the game</li>
 * 	<li>a list of playable species</li>
 * </ul>
 * 
 * The settings editor does not perform the actual saving of the edited module 
 * resource. Instead, it sends a {@code SaveEvent} to request that the module
 * resource be saved.
 * 
 * @author mdriesen
 *
 */
public class SettingsEditor {
	private static final Logger logger = Logger.getGlobal();

	@FXML private Label instructionLabel;
	@FXML private TextField titleField;
	@FXML private ListView<String> speciesList;
	
	private final Stage stage = new Stage();
	private final EventBus bus;
	private final String id;
	private final ResourceManager resources;
	private Scene scene;
	
	/**
	 * Initializes this {@code SettingsEditor}.
	 * 
	 * @param module	the module to edit
	 * @param bus		the {@code EventBus} used for messaging
	 */
	public SettingsEditor(ResourceManager resources, String id, EventBus bus) {
		this.resources = resources;
		this.id = id;
		this.bus = bus;
		
		stage.setTitle("Module properties");
		stage.initModality(Modality.APPLICATION_MODAL); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load settings editor ui");
		}
		
		try {
			RModule module = resources.getResource(id);
			titleField.setText(module.getTitle());
			
			Set<String> creatures = resources.listResources("creatures");
			for (String species : module.getPlayableSpecies()) {
				if (creatures.contains(species)) {
					speciesList.getItems().add(species);
				} else {
					logger.warning("removed unknown creature id: " + species);
				}
			}			
		} catch (ResourceException e) {
			logger.severe("failed to load module resource");
		}
		
		instructionLabel.setText("Providing a game title will overwrite the title given by any parent "
				+ "module. Playable species will be appended to those defined in parent module(s).");
		
		ContextMenu menu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add species");
		addItem.setOnAction(event -> addSpecies(event));
		menu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove species");
		removeItem.setOnAction(event -> removeSpecies(event));
		menu.getItems().add(removeItem);
		speciesList.setContextMenu(menu);
	}
	
	/**
	 * Shows the settings editor window.
	 * 
	 * @param parent the parent window for the dialog window
	 */
	public void show(Window parent) {
		stage.initOwner(parent);
		stage.show();
	}

	@FXML private void cancelPressed(ActionEvent event) {
		stage.close();
	}
	
	@FXML private void applyPressed(ActionEvent event) {
		// save changes to a new module resource
		RModule module = new RModule(id, titleField.getText());
		module.addPlayableSpecies(speciesList.getItems());
		bus.post(new SaveEvent("module", module));
	}
	
	@FXML private void okPressed(ActionEvent event) {
		applyPressed(event);
		cancelPressed(event);
	}

	private void addSpecies(ActionEvent event) {
		Set<String> choices = resources.listResources("creatures");
		choices.removeAll(speciesList.getItems());

		ChoiceDialog<String> dialog = new ChoiceDialog<>(null, choices);
		dialog.setTitle("Add species");
		dialog.setHeaderText("Add a creature to the list of playable species.");
		dialog.setContentText("Choose creature id:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(choice -> speciesList.getItems().add(choice));
	}
	
	private void removeSpecies(ActionEvent event) {
		speciesList.getItems().remove(speciesList.getSelectionModel().getSelectedItem());
	}
}
