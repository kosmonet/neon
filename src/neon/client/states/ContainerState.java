/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

package neon.client.states;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import neon.client.ComponentManager;
import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.common.event.InventoryEvent;
import neon.common.resources.RMap;
import neon.entity.components.Graphics;
import neon.entity.components.Shape;
import neon.entity.entities.Item;

/**
 * A state to handle items when browsing a container or a random heap of 
 * items laying on the ground.
 * 
 * @author mdriesen
 *
 */
public class ContainerState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;

	@FXML private Button cancelButton;
	@FXML private ListView<Long> playerList, containerList;
	@FXML private DescriptionLabel description;
	
	private Scene scene;
	private RMap map;
	
	public ContainerState(UserInterface ui, EventBus bus, ComponentManager components) {
		this.ui = ui;
		this.bus = bus;
		this.components = components;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Container.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		} catch (IOException e) {
			logger.severe("failed to load container interface: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		
		// list catches the esc key, we need a separate listener
		playerList.setOnKeyPressed(event -> keyPressed(event));
		containerList.setOnKeyPressed(event -> keyPressed(event));
		
		// update the item description when another item is selected
		playerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		playerList.focusedProperty().addListener(new FocusListener());
		playerList.setCellFactory(playerList -> new ItemCell());
		containerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		containerList.focusedProperty().addListener(new FocusListener());
		containerList.setCellFactory(playerList -> new ItemCell());
	}
	
	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.F2)) {
			showHelp();
		} else if (event.getCode().equals(KeyCode.ENTER)) {
			drop();
		}
	}
	
	@Subscribe
	private void showInventory(InventoryEvent.List event) {
		playerList.getItems().clear();
		
		for (long uid : event.getItems()) {
			playerList.getItems().add(uid);
		}
		
		playerList.getSelectionModel().selectFirst();
	}
	
	@FXML private void drop() {
		if (playerList.isFocused() && !playerList.getSelectionModel().isEmpty()) {
			long uid = playerList.getSelectionModel().getSelectedItem();
			// we trust the client on this one
			playerList.getItems().remove(uid);
			containerList.getItems().add(uid);
			bus.post(new InventoryEvent.Drop(uid, map.id));
		} else if (containerList.isFocused() && !containerList.getSelectionModel().isEmpty()) {
			long uid = containerList.getSelectionModel().getSelectedItem();
			// we trust the client on this one
			containerList.getItems().remove(uid);
			playerList.getItems().add(uid);
			bus.post(new InventoryEvent.Pick(uid, map.id));			
		}
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering container state");
		bus.post(new InventoryEvent.Request());
		
		Shape shape = event.getParameter(Shape.class);
		map = event.getParameter(RMap.class);
		containerList.getItems().clear();
		for (long uid : map.getEntities(shape.getX(), shape.getY())) {
			containerList.getItems().add(uid);
		}
		containerList.getSelectionModel().selectFirst();
		
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting container state");
	}
	
	@FXML private void showHelp() {
		new HelpWindow().show("inventory.html");
	}
	
	private void updateDescription(long uid) {
    	Graphics graphics = components.getComponent(uid, Graphics.class);
    	Item.Resource resource = components.getComponent(uid, Item.Resource.class);
    	description.update(resource.getResource().name, graphics);
	}
	
	/**
	 * Changes the item description when selecting a different list.
	 * 
	 * @author mdriesen
	 *
	 */
	private class FocusListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (containerList.isFocused()) {
				updateDescription(containerList.getSelectionModel().getSelectedItem());
			} else {
				updateDescription(playerList.getSelectionModel().getSelectedItem());				
			}
		}
	}
	
	/**
	 * Changes the item description when selecting a different item.
	 * 
	 * @author mdriesen
	 *
	 */
	private class ListListener implements ChangeListener<Long> {
		@Override
		public void changed(ObservableValue<? extends Long> observable, Long oldValue, Long newValue) {
			if(newValue != null) {
				updateDescription(newValue);
			}
		}
	}
	
    private class ItemCell extends ListCell<Long> {
    	@Override
    	public void updateItem(Long uid, boolean empty) {
    		super.updateItem(uid, empty);
    		if (empty || uid == null) {
    			setText(null);
    		} else {
    	    	Item.Resource resource = components.getComponent(uid, Item.Resource.class);
    			setText(resource.getResource().name);
    		}
    	}
    }
}
