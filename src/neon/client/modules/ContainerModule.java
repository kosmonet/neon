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

package neon.client.modules;

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
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.common.event.InventoryEvent;
import neon.common.event.NeonEvent;
import neon.common.resources.RMap;
import neon.entity.EntityProvider;
import neon.entity.components.Shape;
import neon.entity.entities.Item;
import neon.entity.entities.Player;

/**
 * A module to handle items when browsing a container or a random heap of 
 * items laying on the ground.
 * 
 * @author mdriesen
 *
 */
public class ContainerModule extends Module {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final EntityProvider entities;

	@FXML private Button cancelButton;
	@FXML private ListView<Item> playerList, containerList;
	@FXML private DescriptionLabel description;
	
	private Scene scene;
	
	public ContainerModule(UserInterface ui, EventBus bus, EntityProvider entities) {
		this.ui = ui;
		this.bus = bus;
		this.entities = entities;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Container.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		} catch (IOException e) {
			logger.severe("failed to load inventory interface: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		
		// list catches the esc key, we need a separate listener
		playerList.setOnKeyPressed(event -> keyPressed(event));
		containerList.setOnKeyPressed(event -> keyPressed(event));
		
		// update the item description when another item is selected
		playerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		playerList.focusedProperty().addListener(new FocusListener());
		containerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		containerList.focusedProperty().addListener(new FocusListener());
	}
	
	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.F2)) {
			showHelp();
		}
	}
	
	@Subscribe
	private void showInventory(InventoryEvent event) {
		playerList.getItems().clear();
		
		for (long item : event.getItems()) {
			playerList.getItems().add(entities.getEntity(item));
		}
		
		playerList.getSelectionModel().selectFirst();
		playerList.requestFocus();
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering inventory module");
		bus.post(new NeonEvent.Inventory());
		
		containerList.getItems().clear();
		Player player = entities.getEntity(0);
		Shape shape = player.getComponent(Shape.class);
		RMap map = event.getParameter("map");
		for (long item : map.getEntities(shape.getX(), shape.getY())) {
			containerList.getItems().add(entities.getEntity(item));
		}
		containerList.getSelectionModel().selectFirst();
		
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting inventory module");
	}
	
	@FXML private void showHelp() {
		new HelpWindow().show("inventory.html");
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
				description.update(containerList.getSelectionModel().getSelectedItem());
			} else {
				description.update(playerList.getSelectionModel().getSelectedItem());				
			}
		}
	}
	
	/**
	 * Changes the item description when selecting a different item.
	 * 
	 * @author mdriesen
	 *
	 */
	private class ListListener implements ChangeListener<Item> {
	    @Override
	    public void changed(ObservableValue<? extends Item> observable, Item oldValue, Item newValue) {
	    	description.update(newValue);
	    }
	}
}
