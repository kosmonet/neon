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
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import neon.client.ComponentManager;
import neon.client.ui.DescriptionLabel;
import neon.client.ui.UserInterface;
import neon.common.event.ComponentEvent;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.magic.Magic;
import neon.systems.magic.MagicEvent;
import neon.systems.magic.RSpell;

public final class MagicState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;

	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;
	private final ResourceManager resources;

	@FXML private ListView<RSpell> spellList;
	@FXML private Label instructionLabel;
	@FXML private DescriptionLabel description;
	
	private Scene scene;

	public MagicState(UserInterface ui, EventBus bus, ComponentManager components, ResourceManager resources) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.components = Objects.requireNonNull(components, "component manager");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Magic.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load magic interface: " + e.getMessage());
		}

		spellList.setOnKeyPressed(this::keyPressed);
		spellList.setCellFactory(spellList -> new SpellCell());
		spellList.getSelectionModel().selectedItemProperty().addListener(new ListListener());

	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering magic state");
		bus.register(this);
		refresh();
		spellList.getSelectionModel().selectFirst();
		ui.showScene(scene);
	}

	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.SPACE)) {
			equipSpell();
		} else if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.F1)) {
			showHelp();
		}
	}
	
	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting magic state");
		bus.unregister(this);
	}
	
	@FXML private void showHelp() {}
	
	@FXML private void equipSpell() {
		String id = spellList.getSelectionModel().getSelectedItem().id;
		Magic magic = components.getComponent(PLAYER_UID, Magic.class);
		
		if (magic.hasEquipped(id)) {
			bus.post(new MagicEvent.Unequip(id));
		} else {
			bus.post(new MagicEvent.Equip(id));	
		}		
	}
	
	@Subscribe
	private void onSpellUpdate(ComponentEvent event) {
		Platform.runLater(this::refresh);
	}
	
	private void refresh() {
		int index = spellList.getSelectionModel().getSelectedIndex();
		spellList.getItems().clear();
		
		for (String id : components.getComponent(PLAYER_UID, Magic.class).getSpells()) {
			try {
				spellList.getItems().add(resources.getResource("spells", id));
			} catch (ResourceException e) {
				logger.warning("could not find spell <" + id + ">");
			}
		}
		
		spellList.getSelectionModel().select(index);
	}
	
	private final class ListListener implements ChangeListener<RSpell> {
	    @Override
	    public void changed(ObservableValue<? extends RSpell> observable, RSpell oldValue, RSpell newValue) {
	    	if (newValue != null) {
	    		description.updateSpell(newValue);	    		
	    	}
	    }
	}

	private final class SpellCell extends ListCell<RSpell> {
		@Override
		public void updateItem(RSpell spell, boolean empty) {
			super.updateItem(spell, empty);
			if (empty || spell == null) {
				setText(null);
			} else {
				setText(spell.name);
				Magic magic = components.getComponent(PLAYER_UID, Magic.class);
				if (magic.hasEquipped(spell.id)) {
					setStyle("-fx-font-weight: bold");
				} else {
					setStyle("-fx-font-weight: normal");
				}
			}
		}
	}
}
