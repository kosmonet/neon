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
import neon.common.event.InventoryEvent;
import neon.common.resources.RItem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.components.Graphics;
import neon.entity.components.Inventory;
import neon.entity.entities.Item;
import neon.util.GraphicsUtils;

public class InventoryState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;
	private final ResourceManager resources;

	@FXML private Button cancelButton;
	@FXML private ListView<Long> playerList, followerList;
	@FXML private DescriptionLabel description;
	@FXML private Label instructionLabel, armorLabel;
	
	private Scene scene;
	private RMap map;
	private Inventory inventory;
	
	public InventoryState(UserInterface ui, EventBus bus, ComponentManager components, ResourceManager resources) {
		this.ui = ui;
		this.bus = bus;
		this.components = components;
		this.resources = resources;
		
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
		map = event.getParameter(RMap.class);
		refresh();		
		playerList.getSelectionModel().selectFirst();		
		ui.showScene(scene);
	}
	
	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting inventory module");
	}
	
	private void refresh() {
		int index = playerList.getSelectionModel().getSelectedIndex();
		int rating = 0;
		inventory = components.getComponent(0, Inventory.class);
		instructionLabel.setText("Money: " + inventory.getMoney() + " copper pieces.");
		playerList.getItems().clear();
		
		for (long uid : inventory.getItems()) {
			playerList.getItems().add(uid);
			
			try {
				if (inventory.hasEquiped(uid)) {
					RItem item = resources.getResource("items", components.getComponent(uid, Item.Resource.class).getID());
					if (item instanceof RItem.Armor) {
						RItem.Armor armor = (RItem.Armor) item;
						rating += armor.rating;
					}
				}
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
		
		armorLabel.setText("Armor rating: " + rating);
		playerList.getSelectionModel().select(index);
	}
	
	@Subscribe
	private void onInventoryUpdate(InventoryEvent.Update event) {
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
				try {
					RItem item = resources.getResource("items", components.getComponent(newValue, Item.Resource.class).getID());
					StringBuilder builder = new StringBuilder();
					builder.append(item.name);
					
					if (item instanceof RItem.Clothing) {
						RItem.Clothing cloth = (RItem.Clothing) item;
						builder.append("\n");
						builder.append("∷");
						builder.append("\n");
						builder.append("Slot: " + cloth.slot.toString().toLowerCase());
					}
					
					if (item instanceof RItem.Armor) {
						RItem.Armor armor = (RItem.Armor) item;
						builder.append("\n");
						builder.append("Rating: " + armor.rating);						
					}
					
					if (item instanceof RItem.Weapon) {
						RItem.Weapon weapon = (RItem.Weapon) item;
						builder.append("\n");
						builder.append("∷");
						builder.append("\n");
						builder.append("Damage: " + weapon.damage);						
					}
					
		    		description.update(builder.toString(), graphics);
				} catch (ResourceException e) {
					logger.warning(e.getMessage());
				}
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
				try {
					RItem item = resources.getResource("items", components.getComponent(uid, Item.Resource.class).getID());
					Color color = inventory.hasEquiped(uid) ? (isSelected() ? Color.TURQUOISE : Color.TEAL) : (isSelected() ? Color.WHITE : Color.SILVER);
	    			setStyle("-fx-text-fill: " + GraphicsUtils.getColorString(color));
	    			setText(item.name);
				} catch (ResourceException e) {
					logger.warning(e.getMessage());
				}
    		}
    	}
    }
}
