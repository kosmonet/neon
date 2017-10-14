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
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.WritableImage;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import neon.editor.resource.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.CreatureLoader;
import neon.editor.resource.CEditor;
import neon.editor.resource.MapLoader;
import neon.editor.Card;
import neon.editor.LoadEvent;
import neon.editor.SaveEvent;
import neon.editor.SelectionEvent;
import neon.editor.ui.CardCellFactory;

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
	@FXML private Slider slider;
	@FXML private ToggleGroup modeGroup;
	
	private final ResourceManager resources;
	private final EventBus bus;
	private final HashSet<Short> uids = new HashSet<>();
	private final CEditor config;
	
	private String namespace, id;	// to keep track of the currently selected resource in the resource pane
	private ImageCursor cursor;
	
	public MapHandler(ResourceManager resources, EventBus bus, CEditor config) {
		this.resources = resources;
		this.bus = bus;
		this.config = config;
	}
	
	@FXML public void initialize() {
		// initialize is run twice (MapPane.fxml and MapTree.fxml), so check first if a node is already initialized
		if(mapTree != null) {
			mapTree.setCellFactory(new CardCellFactory());			
		}
		
		if (slider != null) {
			slider.valueProperty().addListener((observable, oldValue, newValue) -> changeBrushSize(newValue.intValue()));
			changeBrushSize(20);
		}
	}
	
	private void changeBrushSize(int value) {
		Canvas canvas = new Canvas(value, value);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.WHITE);
		gc.strokeRect(0, 0, value, value);

		WritableImage image = new WritableImage(value, value);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);
		canvas.snapshot(parameters, image);
		cursor = new ImageCursor(image, value/2, value/2);

		for (Tab tab : tabs.getTabs()) {
			MapEditor editor = (MapEditor) tab.getUserData();
			editor.setCursor(cursor);
		}
	}
	
	private void loadResources(Multimap<String, Card> cards) {
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
		resources.addLoader("map", new MapLoader(resources));
		resources.addLoader("creature", new CreatureLoader());

		for (Card card : cards.get("maps")) {
			root.getChildren().add(new TreeItem<Card>(card));
			try {
				RMap map = card.getResource();
				uids.add(map.uid);
			} catch (ResourceException e) {
				logger.severe("could not load map: " + card);
			}
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
		Platform.runLater(() -> loadResources(event.getCards()));
	}
	
	@Subscribe 
	private void selectResource(SelectionEvent event) {
		id = event.getID();
		namespace = event.getNamespace();
	}
	
	/**
	 * Asks the user if all opened maps should be saved. This method should be
	 * called when saving a module or exiting the editor.
	 */
	public void saveMaps() {
		if (!tabs.getTabs().isEmpty()) {
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
	
	@FXML private void toggleMode() {
		for (Tab tab : tabs.getTabs()) {
			MapEditor editor = (MapEditor) tab.getUserData();
			editor.setMode(MapEditor.Mode.valueOf(modeGroup.getSelectedToggle().getUserData().toString()));
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
			RMap map = new RMap(id, id, 100, 100, getFreeUID(), config.getActiveModule().id);

			try {
				resources.addResource(map);
				Card card = new Card("maps", id, resources, false);
				card.setChanged(true);
				TreeItem<Card> item = new TreeItem<>(card);
				mapTree.getRoot().getChildren().add(item);
				createTab(card);
			} catch (IOException e) {
				logger.severe("could not create map " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	private short getFreeUID() {
		short uid = 0;
		while(uids.contains(++uid));
		return uid;
	}
	
	private void createTab(Card card) throws ResourceException {
		MapEditor editor = new MapEditor(card, resources, bus);
		editor.setMode(MapEditor.Mode.valueOf(modeGroup.getSelectedToggle().getUserData().toString()));
		editor.setCursor(cursor);
		if (namespace == "terrain") {
			editor.selectTerrain(new SelectionEvent.Terrain(id));
		}
		bus.register(editor);
		Tab tab = new Tab(card.toString(), editor.getPane());
		tab.setUserData(editor);
		tab.setOnClosed(event -> editor.close());
		tab.setId(card.toString());
		tabs.getTabs().add(tab);
		tabs.getSelectionModel().select(tab);
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
						createTab(card);
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
