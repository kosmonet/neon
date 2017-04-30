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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import neon.editor.map.MapEditor;
import neon.editor.ui.CardCellFactory;
import neon.system.resources.RMap;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;
import neon.system.resources.loaders.MapLoader;

/**
 * This handler takes care of loading, saving, adding and removing map 
 * resources.
 * 
 * @author mdriesen
 *
 */
public class MapHandler {
	private final static ButtonType yes = new ButtonType("Yes", ButtonData.OK_DONE);
	private final static ButtonType no = new ButtonType("No", ButtonData.CANCEL_CLOSE);
	private final static Logger logger = Logger.getGlobal();
	
	@FXML private TreeView<Card> mapTree;
	@FXML private TabPane tabs;
	
	private final ResourceManager resources;
	private final EventBus bus;
	
	public MapHandler(ResourceManager resources, EventBus bus) {
		this.resources = resources;
		this.bus = bus;
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
	
	/**
	 * Signals to this handler that something was saved.
	 * 
	 * @param event
	 */
	@Subscribe
	private void save(SaveEvent event) {
		// stuff may still be going on, refresh the tree on the next tick
		Platform.runLater(() -> mapTree.refresh());
	}
	
	/**
	 * Signals to this handler that the entire module was saved.
	 * 
	 * @param event
	 */
	@Subscribe
	private void save(SaveEvent.Module event) {
		mapTree.getRoot().getChildren().forEach(item -> item.getValue().setChanged(false));		
	}
	
	@Subscribe
	private void load(LoadEvent event) {
		// module is loading on this tick, load maps on the next tick
		Platform.runLater(() -> loadResources());
	}
	
	/**
	 * Asks the user if all opened maps should be saved. This method should be
	 * called when saving a module or exiting the editor.
	 */
	void saveMaps() {
		if(!tabs.getTabs().isEmpty()) {
			Alert alert = new Alert(AlertType.CONFIRMATION,
					"Save all opened maps?", yes, no);
			alert.setTitle("Warning");
			alert.setHeaderText("Changes made to opened maps may not have been saved yet.");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){				
				for (Tab tab : tabs.getTabs()) {
					MapEditor editor = (MapEditor) tab.getUserData();
					editor.save();
				}			
			} 
		}
	}
	
	@FXML private void showInfo() {
		Tab tab = tabs.getSelectionModel().getSelectedItem();
		if (tab != null) {
			MapEditor editor = (MapEditor) tab.getUserData();
			editor.showInfo();
		}
	}
	
	@FXML private void showElevation() {
		Tab tab = tabs.getSelectionModel().getSelectedItem();
		if (tab != null) {
			MapEditor editor = (MapEditor) tab.getUserData();
			editor.showElevation();
		}
	}
	
	@FXML private void showTerrain() {
		Tab tab = tabs.getSelectionModel().getSelectedItem();
		if (tab != null) {
			MapEditor editor = (MapEditor) tab.getUserData();
			editor.showTerrain();
		}
	}
	
	@FXML private void save() {
		Tab tab = tabs.getSelectionModel().getSelectedItem();
		if (tab != null) {
			MapEditor editor = (MapEditor) tab.getUserData();
			editor.save();
		}
	}
	
	/**
	 * Adds a map to the currently opened module.
	 * 
	 * @param event
	 */
	private void addMap(ActionEvent event) {
		// ask for a new map id
		TextInputDialog dialog = new TextInputDialog();
		dialog.initOwner(mapTree.getScene().getWindow());
		dialog.setTitle("Add New Map");
		dialog.setHeaderText("Give an id for the new map.");
		dialog.setContentText("Map id:");
		Optional<String> result = dialog.showAndWait();

		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			if (resources.hasResource("maps", id)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.initOwner(mapTree.getScene().getWindow());
				alert.setTitle("Warning");
				alert.setHeaderText("Map conflict");
				alert.setContentText("The map id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the map
			RMap map = new RMap(id, id, 100, 100);

			try {
				resources.addResource("maps", map);
				TreeItem<Card> item = new TreeItem<>(new Card("maps", id, resources));
				mapTree.getRoot().getChildren().add(item);
            	new MapEditor(item.getValue(), resources, bus);
			} catch (IOException e) {
				logger.severe("could not create map " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	/**
	 * Removes a map from the currently opened module.
	 * 
	 * @param event
	 */
	private void removeMap(ActionEvent event) {
		// remove from the map tree
		TreeItem<Card> selected = mapTree.getSelectionModel().getSelectedItem();
		mapTree.getRoot().getChildren().remove(selected);
		Card card = selected.getValue();

		// check if the map was opened in a tab
		Optional<Tab> tab = getTab(card.toString());
		if (tab.isPresent()) {
			// remove from the tab pane
			tabs.getTabs().remove(tab.get());
			// call the onClosed() handler for cleanup
	        tab.get().getOnClosed().handle(null);
		}
		
		// remove from the temp folder
		resources.removeResource("maps", card.toString());
	}
	
	/**
	 * Searches for a tab with the given id.
	 * 
	 * @param id
	 * @return
	 */
	private Optional<Tab> getTab(String id) {
    	for (Tab tab : tabs.getTabs()) {
    		if (tab.getId().equals(id)) {
    			return Optional.of(tab);
    		}
    	}
    	
    	return Optional.empty();
	}
	
	private void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 2) {
            TreeItem<Card> item = mapTree.getSelectionModel().getSelectedItem();
            if (item != null) {
            	Card card = item.getValue();
            	
            	// check if the selected map is already opened
            	Optional<Tab> opt = getTab(card.toString());
            	if (opt.isPresent()) {
            		// if so, just select the tab with that map
           			tabs.getSelectionModel().select(opt.get());
            	} else {
            		// if not, create a new map editor
					try {
						MapEditor editor = new MapEditor(card, resources, bus);
	            		Tab tab = new Tab(card.toString(), editor.getPane());
	            		tab.setUserData(editor);
	            		tab.setOnClosed(event -> editor.close(event));
	            		tab.setId(card.toString());
	            		tabs.getTabs().add(tab);
	            		tabs.getSelectionModel().select(tab);
					} catch (ResourceException e) {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Warning");
						alert.initOwner(mapTree.getScene().getWindow());
						alert.setHeaderText("Resource error");
						alert.setContentText("Could not load map " + card.toString() + "!");
						alert.showAndWait();
					}
            	}
            }
		}		
	}
}
