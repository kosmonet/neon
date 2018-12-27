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
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.Map;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.client.ui.ItemCell;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentEvent;
import neon.common.event.InventoryEvent;

/**
 * A state to handle items when browsing a container or a random heap of 
 * items laying on the ground.
 * 
 * @author mdriesen
 *
 */
public final class ContainerState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	private static final long DUMMY = 1;
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;
	private final Configuration config;

	@FXML private Button cancelButton;
	@FXML private ListView<Long> playerList, containerList;
	@FXML private DescriptionLabel description;
	@FXML private Label moneyLabel;
	
	private Scene scene;
	private Map map;
	private long container = DUMMY;
	
	public ContainerState(UserInterface ui, EventBus bus, ComponentManager components, Configuration config) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.components = Objects.requireNonNull(components, "component manager");
		this.config = Objects.requireNonNull(config, "configuration");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Container.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::showHelp);
		} catch (IOException e) {
			logger.severe("failed to load container interface: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		
		// list catches the esc key, we need a separate listener
		playerList.setOnKeyPressed(this::keyPressed);
		containerList.setOnKeyPressed(this::keyPressed);
		
		// update the item description when another item is selected
		playerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		playerList.focusedProperty().addListener(new FocusListener());
		playerList.setCellFactory(playerList -> new ItemCell(components));
		containerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		containerList.focusedProperty().addListener(new FocusListener());
		containerList.setCellFactory(playerList -> new ItemCell(components));
	}
	
	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.F1)) {
			showHelp();
		} else if (event.getCode().equals(KeyCode.ENTER)) {
			drop();
		}
	}
	
	@Subscribe
	private void onInventoryUpdate(ComponentEvent event) {
		Platform.runLater(this::refresh);
	}
	
	private void refresh() {
		int index = playerList.getSelectionModel().getSelectedIndex();
		playerList.getItems().clear();
		Inventory inventory = components.getComponent(PLAYER_UID, Inventory.class);
		moneyLabel.setText("Money: " + inventory.getMoney() + " copper pieces.");
		
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
			if (container == DUMMY) {
				bus.post(new InventoryEvent.Drop(uid, map.getID()));
			} else {
				bus.post(new InventoryEvent.Store(uid, container));				
			}
		} else if (containerList.isFocused() && !containerList.getSelectionModel().isEmpty()) {
			long uid = containerList.getSelectionModel().getSelectedItem();
			// we trust the client on this one
			containerList.getItems().remove(uid);
			playerList.getItems().add(uid);
			if (container == DUMMY) {
				bus.post(new InventoryEvent.Pick(uid, map.getID()));
			} else {
				bus.post(new InventoryEvent.Take(uid, container));
			}
		}
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering container state");
		bus.register(this);
		
		containerList.getItems().clear();
		map = config.getCurrentMap();
		container = DUMMY;
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		List<Long> entities = map.getEntities(shape.getX(), shape.getY()).stream()
				.filter(uid -> uid != PLAYER_UID).collect(Collectors.toList());
		
		if (entities.size() == 1) {
			long uid = entities.get(0);
			if (components.hasComponent(uid, Inventory.class)) {
				container = uid;
				containerList.getItems().addAll(components.getComponent(uid, Inventory.class).getItems());
			} else {
				containerList.getItems().addAll(entities);
			}
		} else {
			containerList.getItems().addAll(entities);
		}
		
		containerList.getSelectionModel().selectFirst();
		playerList.getSelectionModel().selectFirst();
		refresh();
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
		description.updateItem(components.getComponents(uid));
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
}
