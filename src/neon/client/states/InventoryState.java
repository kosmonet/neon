/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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
import javafx.scene.control.Label;
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
import neon.common.entity.components.Armor;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Weapon;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.InventoryEvent;
import neon.common.resources.RMap;
import neon.util.GraphicsUtils;

public class InventoryState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;

	@FXML private Button cancelButton;
	@FXML private ListView<Long> playerList, followerList;
	@FXML private DescriptionLabel description;
	@FXML private Label instructionLabel, armorLabel;
	
	private Scene scene;
	private RMap map;
	private Inventory inventory;
	
	public InventoryState(UserInterface ui, EventBus bus, ComponentManager components) {
		this.ui = ui;
		this.bus = bus;
		this.components = components;
		
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
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), () -> equipItem());
		
		// lists catch keys, we need a separate listener
		playerList.setOnKeyPressed(event -> keyPressed(event));
		followerList.setOnKeyPressed(event -> keyPressed(event));
		playerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		playerList.setCellFactory(playerList -> new ItemCell());
	}
	
	private void keyPressed(KeyEvent event) {
		switch(event.getCode()) {
		case ESCAPE:
			bus.post(new TransitionEvent("cancel"));
			break;
		case F2:
			showHelp();
			break;
		case ENTER:
			dropItem();
			break;
		case SPACE:
			equipItem();
			break;
		default:
			break;
		}		
	}
	
	@FXML private void equipItem() {
		long uid = playerList.getSelectionModel().getSelectedItem();
		bus.post(new InventoryEvent.Equip(uid));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering inventory module");
		bus.register(this);
		map = event.getParameter(RMap.class);
		refresh();		
		playerList.getSelectionModel().selectFirst();		
		ui.showScene(scene);
	}
	
	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting inventory module");
		bus.unregister(this);
	}
	
	private void refresh() {
		int index = playerList.getSelectionModel().getSelectedIndex();
		int rating = 0;
		inventory = components.getComponent(0, Inventory.class);
		instructionLabel.setText("Money: " + inventory.getMoney() + " copper pieces.");
		playerList.getItems().clear();
		
		for (long uid : inventory.getItems()) {
			playerList.getItems().add(uid);

			if (inventory.hasEquiped(uid)) {
				if (components.hasComponent(uid, Armor.class)) {
					rating += components.getComponent(uid, Armor.class).getRating();
				}
			}
		}
		
		armorLabel.setText("Armor rating: " + rating);
		playerList.getSelectionModel().select(index);
	}
	
	@Subscribe
	private void onInventoryUpdate(ComponentUpdateEvent event) {
		Platform.runLater(() -> refresh());
	}
	
	@FXML private void dropItem() {
		if (!playerList.getSelectionModel().isEmpty()) {
			long uid = playerList.getSelectionModel().getSelectedItem();
			// we trust the client on this one
			playerList.getItems().remove(uid);
			bus.post(new InventoryEvent.Drop(uid, map.id));
		}
	}

	@FXML private void showHelp() {
		new HelpWindow().show("inventory.html");
	}
	
	private class ListListener implements ChangeListener<Long> {
	    @Override
	    public void changed(ObservableValue<? extends Long> observable, Long oldValue, Long newValue) {
	    	if (newValue != null) {
	    		Graphics graphics = components.getComponent(newValue, Graphics.class);
	    		ItemInfo info = components.getComponent(newValue, ItemInfo.class);
	    		StringBuilder builder = new StringBuilder();
	    		builder.append(info.getName());
    			builder.append("\n");

	    		if (components.hasComponent(newValue, Clothing.class)) {
	    			Clothing clothing = components.getComponent(newValue, Clothing.class);
	    			builder.append("∷\n");
	    			builder.append("Slot: " + clothing.getSlot().toString().toLowerCase());
	    		}

	    		if (components.hasComponent(newValue, Armor.class)) {
	    			Armor armor = components.getComponent(newValue, Armor.class);
	    			builder.append("\n");
	    			builder.append("Rating: " + armor.getRating());						
	    		}

	    		if (components.hasComponent(newValue, Weapon.class)) {
	    			Weapon weapon = components.getComponent(newValue, Weapon.class);
	    			builder.append("∷\n");
	    			builder.append("Damage: " + weapon.getDamage());						
	    		}

	    		description.update(builder.toString(), graphics);
	    	}
	    }
	}

    private class ItemCell extends ListCell<Long> {
    	@Override
    	public void updateItem(Long uid, boolean empty) {
    		super.updateItem(uid, empty);
    		
    		if (empty || uid == null) {
    			setGraphic(null);
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
