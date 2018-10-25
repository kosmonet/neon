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

import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import neon.client.ComponentManager;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.InventoryEvent;
import neon.common.resources.RMap;
import neon.util.GraphicsUtils;

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
	private Inventory inventory;
	
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
	private void onInventoryUpdate(ComponentUpdateEvent event) {
		Platform.runLater(() -> refresh());
	}
	
	private void refresh() {
		int index = playerList.getSelectionModel().getSelectedIndex();
		inventory = components.getComponent(0, Inventory.class);
		playerList.getItems().clear();
		
		for (long uid : inventory.getItems()) {
			playerList.getItems().add(uid);
		}
		playerList.getSelectionModel().select(index);
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
		bus.register(this);
		Shape shape = components.getComponent(0, Shape.class);
		map = event.getParameter(RMap.class);
		containerList.getItems().clear();
		for (long uid : map.getEntities(shape.getX(), shape.getY())) {
			if (uid != 0) {
				containerList.getItems().add(uid);
			}
		}
		containerList.getSelectionModel().selectFirst();
		refresh();
		playerList.getSelectionModel().selectFirst();

		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting container state");
		bus.unregister(this);
	}
	
	@FXML private void showHelp() {
		new HelpWindow().show("inventory.html");
	}
	
	private void updateDescription(long uid) {
		Graphics graphics = components.getComponent(uid, Graphics.class);
		ItemInfo info = components.getComponent(uid, ItemInfo.class);
		description.update(info.getName(), graphics);
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
			if (containerList.isFocused() && !containerList.getItems().isEmpty()) {
				if (containerList.getSelectionModel().getSelectedIndex() < 0) {
					containerList.getSelectionModel().selectFirst();
				}
				updateDescription(containerList.getSelectionModel().getSelectedItem());
			} else if (playerList.isFocused() && !playerList.getItems().isEmpty()) {
				if (playerList.getSelectionModel().getSelectedIndex() < 0) {
					playerList.getSelectionModel().selectFirst();
				}
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
			if (newValue != null) {
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
				ItemInfo info = components.getComponent(uid, ItemInfo.class);
				Color color = inventory.hasEquiped(uid) ? (isSelected() ? Color.TURQUOISE : Color.TEAL) : (isSelected() ? Color.WHITE : Color.SILVER);
				setStyle("-fx-text-fill: " + GraphicsUtils.getColorString(color));
				setText(info.getName());
			}
		}
	}
}
