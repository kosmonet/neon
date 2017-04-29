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
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import neon.editor.dialogs.CreatureEditor;
import neon.editor.ui.CardCellFactory;
import neon.system.resources.CreatureLoader;
import neon.system.resources.RCreature;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

/**
 * Class to handle all creature-related matters.
 * 
 * @author mdriesen
 *
 */
public class CreatureHandler {
	private final static Logger logger = Logger.getGlobal();
	
	@FXML private TreeView<Card> creatureTree;
	private final ResourceManager resources;
	private final EventBus bus;
	private Window parent;
	
	CreatureHandler(ResourceManager resources, EventBus bus) {
		this.resources = resources;
		this.bus = bus;
	}
	
	@FXML public void initialize() {		
		creatureTree.setCellFactory(new CardCellFactory());
	}
	
	/**
	 * Populates the creature tree with creatures.
	 */
	private void loadResources() {
		ContextMenu creatureMenu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add creature");
		addItem.setOnAction(event -> addCreature(event));
		creatureMenu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove creature");
		removeItem.setOnAction(event -> removeCreature(event));
		creatureMenu.getItems().add(removeItem);
		creatureTree.setContextMenu(creatureMenu);

		parent = creatureTree.getScene().getWindow();

		TreeItem<Card> root = new TreeItem<>();
		creatureTree.setShowRoot(false);
		creatureTree.setRoot(root);
		creatureTree.setOnMouseClicked(event -> mouseClicked(event));
		resources.addLoader("creature", new CreatureLoader());

		for (String creature : resources.listResources("creatures")) {
			TreeItem<Card> item = new TreeItem<>(new Card("creatures", creature, resources));
			root.getChildren().add(item);
		}
	}
	
	/**
	 * Signals to this handler that something was saved.
	 * 
	 * @param event
	 */
	@Subscribe
	private void save(SaveEvent event) {
		// stuff may still be going on, refresh the tree on the next tick
		Platform.runLater(() -> creatureTree.refresh());
	}
	
	/**
	 * Signals to this handler that the entire module was saved.
	 * 
	 * @param event
	 */
	@Subscribe
	private void save(SaveEvent.Module event) {
		creatureTree.getRoot().getChildren().forEach(item -> item.getValue().setChanged(false));		
	}
	
	/**
	 * Signals to this handler that a module was loaded.
	 * 
	 * @param event
	 */
	@Subscribe
	private void load(LoadEvent event) {
		// module is loading on this tick, load creatures on the next tick
		Platform.runLater(() -> loadResources());
	}
	
	/**
	 * Adds a creature to the module.
	 * 
	 * @param event
	 */
	private void addCreature(ActionEvent event) {
		// ask for a new creature id
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New Creature");
		dialog.setHeaderText("Give an id for the new creature.");
		dialog.setContentText("Creature id:");
		Optional<String> result = dialog.showAndWait();

		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			if (resources.hasResource("creatures", id)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Creature conflict");
				alert.setContentText("The creature id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the creature
			RCreature creature = new RCreature(id, id);

			try {
				resources.addResource("creatures", creature);
				TreeItem<Card> item = new TreeItem<>(new Card("creatures", id, resources));
				creatureTree.getRoot().getChildren().add(item);
            	new CreatureEditor(item.getValue(), bus).show(parent);
			} catch (IOException e) {
				logger.severe("could not create creature " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	/**
	 * Removes a creature from the module.
	 * 
	 * @param event
	 */
	private void removeCreature(ActionEvent event) {
		// remove from the creature tree
		TreeItem<Card> selected = creatureTree.getSelectionModel().getSelectedItem();
		creatureTree.getRoot().getChildren().remove(selected);
		// remove from the temp folder
		resources.removeResource("creatures", selected.getValue().toString());
	}
	
	/**
	 * Opens a creature editor when a creature in the tree was double clicked.
	 * 
	 * @param event
	 */
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<Card> item = creatureTree.getSelectionModel().getSelectedItem();
            if (item != null) {
	            try {
					new CreatureEditor(item.getValue(), bus).show(parent);
				} catch (ResourceException e) {
					logger.severe(e.getMessage());
				}
            }
        }
	}	
}