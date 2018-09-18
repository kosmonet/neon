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
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.common.event.InventoryEvent;
import neon.common.resources.RMap;
import neon.entity.EntityProvider;
import neon.entity.entities.Item;

public class InventoryState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final EntityProvider entities;

	@FXML private Button cancelButton;
	@FXML private ListView<Item> playerList, followerList;
	@FXML private DescriptionLabel description;
	
	private Scene scene;
	private RMap map;
	
	public InventoryState(UserInterface ui, EventBus bus, EntityProvider entities) {
		this.ui = ui;
		this.bus = bus;
		this.entities = entities;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Inventory.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load inventory interface: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		
		// list catches the esc and enter keys, we need a separate listener
		playerList.setOnKeyPressed(event -> keyPressed(event));
		followerList.setOnKeyPressed(event -> keyPressed(event));
		playerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
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
		
		for (long item : event.getItems()) {
			playerList.getItems().add(entities.getEntity(item));
		}
		
		playerList.getSelectionModel().selectFirst();
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering inventory module");
		bus.post(new InventoryEvent.Request());
		map = event.getParameter(RMap.class);
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting inventory module");
	}
	
	@FXML private void drop() {
		if (!playerList.getSelectionModel().isEmpty()) {
			Item item = playerList.getSelectionModel().getSelectedItem();
			// we trust the client on this one
			playerList.getItems().remove(item);
			bus.post(new InventoryEvent.Drop(item.uid, map.id));
		}
	}

	@FXML private void showHelp() {
		new HelpWindow().show("inventory.html");
	}
	
	private class ListListener implements ChangeListener<Item> {
	    @Override
	    public void changed(ObservableValue<? extends Item> observable, Item oldValue, Item newValue) {
	    	description.update(newValue);
	    }
	}
}
