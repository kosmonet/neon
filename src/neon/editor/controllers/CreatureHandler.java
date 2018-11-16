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

package neon.editor.controllers;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.Multimap;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import neon.common.resources.RCreature;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.editor.Card;
import neon.editor.LoadEvent;
import neon.editor.SaveEvent;
import neon.editor.dialogs.CreatureEditor;
import neon.editor.ui.CardCellFactory;

/**
 * Class to handle all creature-related matters.
 * 
 * @author mdriesen
 *
 */
public class CreatureHandler {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private TreeView<Card> creatureTree;
	
	private final ResourceManager resources;
	private final EventBus bus;
	
	private Window parent;
	
	public CreatureHandler(ResourceManager resources, EventBus bus) {
		this.resources = resources;
		this.bus = bus;
	}
	
	@FXML public void initialize() {		
		creatureTree.setCellFactory(new CardCellFactory());
		creatureTree.setOnDragDetected(event -> mouseDragged(event));
	}

	private void mouseDragged(MouseEvent event) {
		Dragboard db = creatureTree.startDragAndDrop(TransferMode.ANY);	        
		ClipboardContent content = new ClipboardContent();
		content.putString(creatureTree.getSelectionModel().getSelectedItem().getValue().toString());
		db.setContent(content);
		event.consume();
	}		
	
	/**
	 * Populates the creature tree with creatures.
	 */
	private void loadResources(Multimap<String, Card> cards) {
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

		for (Card card : cards.get("creatures")) {
			root.getChildren().add(new TreeItem<Card>(card));
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
		Platform.runLater(() -> loadResources(event.getCards()));
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
			try {
				resources.addResource(new RCreature.Builder(id).setName(id).setGraphics('?', Color.BLUE).
						setSpeed(10).setStats(10, 10, 10, 10, 10, 10).build());
				Card card = new Card("creatures", id, resources, false);
				card.setChanged(true);
				TreeItem<Card> item = new TreeItem<>(card);
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