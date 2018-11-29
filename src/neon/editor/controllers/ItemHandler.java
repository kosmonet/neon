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
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import neon.common.resources.RItem;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.editor.Card;
import neon.editor.LoadEvent;
import neon.editor.SaveEvent;
import neon.editor.dialogs.ItemEditor;
import neon.editor.ui.CardCellFactory;

public class ItemHandler {
	private static final Logger logger = Logger.getGlobal();

	@FXML private TreeView<Card> itemTree;
	
	private final ResourceManager resources;
	private final EventBus bus;
	private final TreeItem<Card> items = new TreeItem<>(new Card.Type("Items"));
	private final TreeItem<Card> doors = new TreeItem<>(new Card.Type("Doors"));
	private final TreeItem<Card> containers = new TreeItem<>(new Card.Type("Containers"));
	
	private Window parent;

	public ItemHandler(ResourceManager resources, EventBus bus) {
		this.resources = resources;
		this.bus = bus;
	}
	
	@FXML public void initialize() {		
		itemTree.setCellFactory(new CardCellFactory());
	}
	
	private void loadResources(Multimap<String, Card> cards) {
		ContextMenu itemMenu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add item");
		addItem.setOnAction(this::addItem);
		itemMenu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove item");
		removeItem.setOnAction(this::removeItem);
		itemMenu.getItems().add(removeItem);
		itemTree.setContextMenu(itemMenu);

		TreeItem<Card> root = new TreeItem<>();
		itemTree.setShowRoot(false);
		itemTree.setRoot(root);
		itemTree.setOnMouseClicked(this::mouseClicked);
		
		root.getChildren().add(items);
		root.getChildren().add(doors);
		root.getChildren().add(containers);

		for (Card card : cards.get("items")) {
			items.getChildren().add(new TreeItem<Card>(card));
		}
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
	 * Signals to this handler that something was saved.
	 * 
	 * @param event
	 */
	@Subscribe
	private void save(SaveEvent event) {
		// stuff may still be going on, refresh the tree on the next tick
		Platform.runLater(itemTree::refresh);
	}
	
	/**
	 * Adds an item to the module.
	 * 
	 * @param event
	 */
	private void addItem(ActionEvent event) {
		// ask for a new item id
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New Item");
		dialog.setHeaderText("Give an id for the new item.");
		dialog.setContentText("Item id:");
		Optional<String> result = dialog.showAndWait();

		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			if (resources.hasResource("items", id)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Item conflict");
				alert.setContentText("The item id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the item
			try {
				RItem.Builder builder = new RItem.Builder(id, id).setGraphics('?', Color.BLUE);
				resources.addResource(builder.build());
				Card card = new Card("items", id, resources, false);
				card.setChanged(true);
				TreeItem<Card> item = new TreeItem<>(card);
				items.getChildren().add(item);
            	new ItemEditor(item.getValue(), bus).show(parent);
			} catch (IOException e) {
				logger.severe("could not create item " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	/**
	 * Removes an item from the module.
	 * 
	 * @param event
	 */
	private void removeItem(ActionEvent event) {
		// remove from the item tree
		TreeItem<Card> selected = itemTree.getSelectionModel().getSelectedItem();
		selected.getParent().getChildren().remove(selected);
		// remove from the temp folder
		resources.removeResource("items", selected.getValue().toString());
	}
	
	/**
	 * Opens an item editor when an item in the tree was double clicked.
	 * 
	 * @param event
	 */
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<Card> item = itemTree.getSelectionModel().getSelectedItem();
            // don't react when clicked on the item type headings
            if (item != null && !(item.getValue() instanceof Card.Type)) {
	            try {
					new ItemEditor(item.getValue(), bus).show(parent);
				} catch (ResourceException e) {
					logger.severe(e.getMessage());
				}
            }
        }
	}
}
