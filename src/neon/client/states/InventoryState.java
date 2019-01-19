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
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import neon.client.ClientUtils;
import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.client.ui.ItemCell;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentEvent;
import neon.common.event.InventoryEvent;
import neon.common.resources.Slot;
import neon.systems.combat.Armor;
import neon.systems.combat.Weapon;
import neon.systems.magic.Enchantment;
import neon.systems.magic.MagicEvent;

/**
 * A state to let the player handle their inventory.
 * 
 * @author mdriesen
 *
 */
public final class InventoryState extends State {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;
	private final Configuration config;

	@FXML private Button cancelButton;
	@FXML private ListView<Long> playerList, followerList;
	@FXML private DescriptionLabel description;
	@FXML private Label weightLabel, armorLabel, moneyLabel;
	
	private Scene scene;
	
	public InventoryState(UserInterface ui, EventBus bus, ComponentManager components, Configuration config) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.components = Objects.requireNonNull(components, "component manager");
		this.config = Objects.requireNonNull(config, "configuration");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Inventory.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			LOGGER.severe("failed to load inventory interface: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::showHelp);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), this::equipItem);
		
		// lists catch keys, we need a separate listener
		playerList.setOnKeyPressed(this::keyPressed);
		followerList.setOnKeyPressed(this::keyPressed);
		playerList.getSelectionModel().selectedItemProperty().addListener(new ListListener());
		playerList.setCellFactory(playerList -> new ItemCell(components));
	}
	
	private void keyPressed(KeyEvent event) {
		switch(event.getCode()) {
		case ESCAPE:
			bus.post(new TransitionEvent("cancel"));
			break;
		case F1:
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
		Equipment equipment = components.getComponent(Configuration.PLAYER_UID, Equipment.class);
		
		if (equipment.hasEquipped(uid)) {
			bus.post(new InventoryEvent.Unequip(uid));
		} else {
			if (components.hasComponent(uid,  Weapon.class)) {
				bus.post(new InventoryEvent.Equip(uid, selectSlot()));				
			} else if (components.hasComponent(uid, Clothing.class)) {
				bus.post(new InventoryEvent.Equip(uid));
			} else if (components.hasComponent(uid, Enchantment.class)) {
				// if it's no weapon or clothing, but has an enchantment, it must be a potion
				bus.post(new MagicEvent.Drink(uid, Configuration.PLAYER_UID));
			}
		}
	}

	private Slot selectSlot() {
		HashMap<ButtonType, Slot> mapping = new HashMap<>();

		ButtonType left = new ButtonType("left hand");
		mapping.put(left, Slot.HAND_LEFT);
		ButtonType right = new ButtonType("right hand");
		mapping.put(right, Slot.HAND_RIGHT);

		Optional<ButtonType> result = ui.showQuestion("Equip in which hand?", left, right);
		return mapping.get(result.orElse(right));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		LOGGER.finest("entering inventory module");
		bus.register(this);
		config.getCurrentMap();
		refresh();		
		playerList.getSelectionModel().selectFirst();		
		ui.showScene(scene);
	}
	
	@Override
	public void exit(TransitionEvent event) {
		LOGGER.finest("exiting inventory module");
		bus.unregister(this);
	}
	
	private void refresh() {
		int index = playerList.getSelectionModel().getSelectedIndex();
		playerList.getItems().clear();

		Inventory inventory = components.getComponent(Configuration.PLAYER_UID, Inventory.class);
		moneyLabel.setText("Money: " + inventory.getMoney() + " copper pieces");
		
		Stats stats = components.getComponent(Configuration.PLAYER_UID, Stats.class);
		int weight = ClientUtils.getWeight(inventory, components);
		weightLabel.setText("Encumbrance: " + weight + " of " + 6*stats.getBaseStr()+ "/" + 9*stats.getBaseStr() + " stone");

		int rating = 0;
		Equipment equipment = components.getComponent(Configuration.PLAYER_UID, Equipment.class);
		for (long uid : inventory.getItems()) {
			playerList.getItems().add(uid);
			if (equipment.hasEquipped(uid) && components.hasComponent(uid, Armor.class)) {
				Armor armor = components.getComponent(uid, Armor.class);
				Clothing clothing = components.getComponent(uid, Clothing.class);
				rating += armor.getRating()*clothing.getSlot().modifier;
			}
		}

		armorLabel.setText("Armor rating: " + rating);
		playerList.getSelectionModel().select(index);
	}
	
	@Subscribe
	private void onInventoryUpdate(ComponentEvent event) {
		Platform.runLater(this::refresh);
	}
	
	@FXML private void dropItem() {
		if (!playerList.getSelectionModel().isEmpty()) {
			long uid = playerList.getSelectionModel().getSelectedItem();
			// we trust the client on this one
			playerList.getItems().remove(uid);
			bus.post(new InventoryEvent.Drop(uid, config.getCurrentMap().getId()));
		}
	}

	@FXML private void showHelp() {
		new HelpWindow().show("inventory.html");
	}
	
	/**
	 * A custom {@code ChangeListener} to update the item description when a
	 * new item is selected.
	 * 
	 * @author mdriesen
	 *
	 */
	private final class ListListener implements ChangeListener<Long> {
	    @Override
	    public void changed(ObservableValue<? extends Long> observable, Long oldValue, Long newValue) {
	    	if (newValue != null) {
	    		description.updateItem(components.getComponents(newValue));	    		
	    	}
	    }
	}
}
