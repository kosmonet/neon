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

package neon.editor.dialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import neon.common.resources.RModule;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.editor.SaveEvent;
import neon.editor.help.HelpWindow;

/**
 * This resource editor shows a modal dialog window to edit the properties
 * of a module:
 * <ul>
 * 	<li>the title of the game</li>
 * 	<li>the start position of the player character</li>
 * 	<li>a list of parent modules</li>
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
	@FXML private TextField titleField, subtitleField, mapField;
	@FXML private ListView<String> speciesList, parentList;
	@FXML private Spinner<Integer> xSpinner, ySpinner;
	
	private final Stage stage = new Stage();
	private final EventBus bus;
	private final String id;
	private final ResourceManager resources;
	
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
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../ui/editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load settings editor ui: " + e.getMessage());
		}
		
		try {
			RModule module = resources.getResource(id);
			titleField.setText(module.title);
			subtitleField.setText(module.subtitle);
			
			mapField.setText(module.map);
			xSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE));
			xSpinner.getValueFactory().setValue(module.x);
			ySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE));
			ySpinner.getValueFactory().setValue(module.y);
			
			Set<String> creatures = resources.listResources("creatures");
			for (String species : module.getPlayableSpecies()) {
				if (creatures.contains(species)) {
					speciesList.getItems().add(species);
				} else {
					logger.warning("removed unknown creature id: " + species);
				}
			}
			
			for (String parent : module.getParentModules()) {
				parentList.getItems().add(parent);
			}
		} catch (ResourceException e) {
			logger.severe("failed to load module resource");
		}
		
		instructionLabel.setText("WARNING: adding or removing parent modules "
				+ "requires a restart of the editor.");
		
		ContextMenu speciesMenu = new ContextMenu();
		MenuItem addCreatureItem = new MenuItem("Add species");
		addCreatureItem.setOnAction(this::addSpecies);
		speciesMenu.getItems().add(addCreatureItem);
		MenuItem removeCreatureItem = new MenuItem("Remove species");
		removeCreatureItem.setOnAction(this::removeSpecies);
		speciesMenu.getItems().add(removeCreatureItem);
		speciesList.setContextMenu(speciesMenu);

		ContextMenu parentMenu = new ContextMenu();
		MenuItem addParentItem = new MenuItem("Add parent");
		addParentItem.setOnAction(this::addParent);
		parentMenu.getItems().add(addParentItem);
		MenuItem removeParentItem = new MenuItem("Remove parent");
		removeParentItem.setOnAction(this::removeParent);
		parentMenu.getItems().add(removeParentItem);
		parentList.setContextMenu(parentMenu);
		
		speciesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		parentList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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
	
	/**
	 * Saves the changes made to the module settings.
	 * 
	 * @param event
	 */
	@FXML private void applyPressed(ActionEvent event) {
		// save changes to a new module resource
		String title = titleField.getText();
		String subtitle = subtitleField.getText();
		String map = mapField.getText();
		xSpinner.increment(0);
		ySpinner.increment(0);
		RModule.Builder builder = new RModule.Builder(id).setTitle(title).setSubtitle(subtitle);
		builder.setStartMap(map).setStartPosition(xSpinner.getValue(), ySpinner.getValue());
		speciesList.getItems().forEach(builder::addStartItem);
		parentList.getItems().forEach(builder::addParentModule);
		// an RModule is in the global namespace
		bus.post(new SaveEvent.Resources(builder.build()));
	}
	
	@FXML private void okPressed(ActionEvent event) {
		applyPressed(event);
		cancelPressed(event);
	}

	/**
	 * Shows the help window.
	 * 
	 * @param event
	 */
	@FXML private void helpPressed(ActionEvent event) {
		new HelpWindow().show("settings.html");
	}
	
	/**
	 * Moves a parent module up in the list.
	 * 
	 * @param event
	 */
	@FXML private void moveUp(ActionEvent event) {
		int index = parentList.getSelectionModel().getSelectedIndex();
		String parent = parentList.getSelectionModel().getSelectedItem();
		if (parent != null && index > 0) {
			parentList.getItems().remove(parent);
			parentList.getItems().add(index - 1, parent);
			parentList.getSelectionModel().select(parent);
		}
	}

	/**
	 * Moves a parent module down in the list.
	 * 
	 * @param event
	 */
	@FXML private void moveDown(ActionEvent event) {
		int index = parentList.getSelectionModel().getSelectedIndex();
		String parent = parentList.getSelectionModel().getSelectedItem();
		if (parent != null && index < parentList.getItems().size() - 1) {
			parentList.getItems().remove(parent);
			parentList.getItems().add(index + 1, parent);
			parentList.getSelectionModel().select(parent);
		}		
	}

	/**
	 * Adds a playable species.
	 * 
	 * @param event
	 */
	private void addSpecies(ActionEvent event) {
		Set<String> choices = resources.listResources("creatures");
		choices.removeAll(speciesList.getItems());

		ChoiceDialog<String> dialog = new ChoiceDialog<>(null, choices);
		dialog.setTitle("Add species");
		dialog.setHeaderText("Add a creature to the list of playable species.");
		dialog.initOwner(stage);
		dialog.setContentText("Choose creature id:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(speciesList.getItems()::add);
	}
	
	/**
	 * Removes a playable species.
	 * 
	 * @param event
	 */
	private void removeSpecies(ActionEvent event) {
		speciesList.getItems().remove(speciesList.getSelectionModel().getSelectedItem());
	}

	/**
	 * Adds a parent module.
	 * 
	 * @param event
	 */
	private void addParent(ActionEvent event) {
		Path data = Paths.get("data");
		Set<String> choices = Collections.emptySet();
		try (Stream<Path> paths = Files.list(data)) {
			choices = paths.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
		} catch (IOException e) {
			logger.severe("could not list all modules");
		}

		choices.removeAll(parentList.getItems());
		choices.remove(id);

		ChoiceDialog<String> dialog = new ChoiceDialog<>(null, choices);
		dialog.setTitle("Add parent module");
		dialog.setHeaderText(null);
		dialog.initOwner(stage);
		dialog.setContentText("Select parent folder:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(parentList.getItems()::add);
	}
	
	/**
	 * Removes a parent module.
	 * 
	 * @param event
	 */
	private void removeParent(ActionEvent event) {
		parentList.getItems().remove(parentList.getSelectionModel().getSelectedItem());
	}
}
