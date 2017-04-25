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
import neon.editor.map.MapEditor;
import neon.system.resources.MapLoader;
import neon.system.resources.RMap;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

public class MapHandler {
	private final static Logger logger = Logger.getGlobal();
	
	@FXML private TreeView<Card> mapTree;
	
	private final ResourceManager resources;
	
	public MapHandler(ResourceManager resources) {
		this.resources = resources;
	}
	
	@FXML public void initialize() {
		mapTree.setCellFactory(new CardCellFactory());		
	}
	
	private void loadResources() {
		ContextMenu mapMenu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add map");
		addItem.setOnAction(event -> addMap(event));
		mapMenu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove map");
		removeItem.setOnAction(event -> removeMap(event));
		mapMenu.getItems().add(removeItem);
		mapTree.setContextMenu(mapMenu);
		
		TreeItem<Card> root = new TreeItem<>();
		mapTree.setShowRoot(false);
		mapTree.setRoot(root);
		mapTree.setOnMouseClicked(event -> mouseClicked(event));
		resources.addLoader("map", new MapLoader());

		for (String map : resources.listResources("maps")) {
			TreeItem<Card> item = new TreeItem<>(new Card("maps", map, resources));
			root.getChildren().add(item);
		}
	}
	
	@Subscribe
	private void save(SaveEvent event) {
		switch(event.toString()) {
		case "maps":
			// tree cells don't automatically refresh if content is changed
			mapTree.refresh();
			break;
		case "module":
			// reset the changed status for all index cards
			mapTree.getRoot().getChildren().forEach(item -> item.getValue().setChanged(false));
			mapTree.refresh();
			break;
		}
	}
	
	@Subscribe
	private void load(LoadEvent event) {
		// editor is loading on this tick, schedule a refresh on the next tick
		Platform.runLater(() -> loadResources());
	}
	
	private void addMap(ActionEvent event) {
		// ask for a new map id
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New Map");
		dialog.setHeaderText("Give an id for the new map.");
		dialog.setContentText("Map id:");
		Optional<String> result = dialog.showAndWait();

		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			if (resources.hasResource("maps", id)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Map conflict");
				alert.setContentText("The map id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the map
			RMap map = new RMap(id);

			try {
				resources.addResource("maps", map);
				TreeItem<Card> item = new TreeItem<>(new Card("maps", id, resources));
				mapTree.getRoot().getChildren().add(item);
//            	new MapEditor(item.getValue());
			} catch (IOException e) {
				logger.severe("could not create map " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	private void removeMap(ActionEvent event) {
		// remove from the map tree
		TreeItem<Card> selected = mapTree.getSelectionModel().getSelectedItem();
		mapTree.getRoot().getChildren().remove(selected);
		// remove from the temp folder
		resources.removeResource("maps", selected.getValue().toString());
	}
	
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<Card> item = mapTree.getSelectionModel().getSelectedItem();
            if (item != null) {
            	new MapEditor(item.getValue());
            }
        }		
	}
}
