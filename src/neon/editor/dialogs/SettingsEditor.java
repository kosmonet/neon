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
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import neon.editor.SaveEvent;
import neon.system.resources.RModule;

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
	private Scene scene;
	
	/**
	 * Initializes this {@code SettingsEditor}.
	 * 
	 * @param module	the module to edit
	 * @param mainStage	the parent stage for the dialog window
	 * @param bus		the {@code EventBus} used for messaging
	 */
	public SettingsEditor(RModule module, Stage mainStage, EventBus bus) {
		this.id = module.getID();
		this.bus = bus;
		
		stage.initOwner(mainStage);
		stage.setTitle("Module settings");
		stage.initModality(Modality.APPLICATION_MODAL); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			logger.severe("failed to load settings editor ui");
		}
		
		titleField.setText(module.getTitle());
		speciesList.getItems().addAll(module.getPlayableSpecies());
		
		instructionLabel.setText("Providing a game title will overwrite the title given by any parent "
				+ "module(s). Playable species will be appended to those defined in parent module(s).");
	}
	
	/**
	 * Shows the settings editor window.
	 */
	public void show() {
		stage.showAndWait();
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
}
