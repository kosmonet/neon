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
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.TerrainLoader;
import neon.editor.Card;
import neon.editor.LoadEvent;
import neon.editor.SelectionEvent;
import neon.editor.dialogs.TerrainEditor;
import neon.editor.ui.CardCellFactory;

public class TerrainHandler {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private TreeView<Card> terrainTree;
	
	private final ResourceManager resources;
	private final EventBus bus;
	private Window parent;
	
	public TerrainHandler(ResourceManager resources, EventBus bus) {
		this.resources = resources;
		this.bus = bus;
	}
	
	@FXML public void initialize() {		
		terrainTree.setCellFactory(new CardCellFactory());
	}
	
	private void loadResources(Multimap<String, Card> cards) {
		ContextMenu terrainMenu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add terrain");
		addItem.setOnAction(event -> addTerrain(event));
		terrainMenu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove terrain");
		removeItem.setOnAction(event -> removeTerrain(event));
		terrainMenu.getItems().add(removeItem);
		terrainTree.setContextMenu(terrainMenu);

		parent = terrainTree.getScene().getWindow();

		TreeItem<Card> root = new TreeItem<>();
		terrainTree.setShowRoot(false);
		terrainTree.setRoot(root);
		terrainTree.setOnMouseClicked(event -> mouseClicked(event));
		resources.addLoader("terrain", new TerrainLoader());

		for (Card card : cards.get("terrain")) {
			root.getChildren().add(new TreeItem<Card>(card));
		}
	}
	
	/**
	 * Signals to this handler that a module was loaded.
	 * 
	 * @param event
	 */
	@Subscribe
	private void load(LoadEvent event) {
		// module is loading on this tick, load terrains on the next tick
		Platform.runLater(() -> loadResources(event.getCards()));
	}
	
	/**
	 * Adds a terrain type to the module.
	 * 
	 * @param event
	 */
	private void addTerrain(ActionEvent event) {
		// ask for a new terrain id
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New Terrain");
		dialog.setHeaderText("Give an id for the new terrain type.");
		dialog.setContentText("terrain type id:");
		Optional<String> result = dialog.showAndWait();

		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			if (resources.hasResource("terrain", id)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Terrain conflict");
				alert.setContentText("The terrain type id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the terrain
			RTerrain terrain = new RTerrain(id, id, '.', Color.WHITE);

			try {
				resources.addResource(terrain);
				TreeItem<Card> item = new TreeItem<>(new Card("terrain", id, resources, false));
				terrainTree.getRoot().getChildren().add(item);
            	new TerrainEditor(item.getValue(), bus).show(parent);
			} catch (IOException e) {
				logger.severe("could not create terrain " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	/**
	 * Removes a terrain type from the module.
	 * 
	 * @param event
	 */
	private void removeTerrain(ActionEvent event) {
		// remove from the terrain tree
		TreeItem<Card> selected = terrainTree.getSelectionModel().getSelectedItem();
		terrainTree.getRoot().getChildren().remove(selected);
		// remove from the temp folder
		resources.removeResource("terrain", selected.getValue().toString());
	}
	
	/**
	 * Opens a terrain editor when an item in the tree was double clicked.
	 * 
	 * @param event
	 */
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<Card> item = terrainTree.getSelectionModel().getSelectedItem();
            if (item != null) {
	            try {
					new TerrainEditor(item.getValue(), bus).show(parent);
				} catch (ResourceException e) {
					logger.severe(e.getMessage());
				}
            }
		} else {
			// also send out a message that something in the tree was selected
			TreeItem<Card> item = terrainTree.getSelectionModel().getSelectedItem();
			if (item != null) {
				bus.post(new SelectionEvent.Terrain(item.getValue().toString()));
			}
		}
	}
}
