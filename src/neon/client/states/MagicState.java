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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import neon.client.ComponentManager;
import neon.client.ui.UserInterface;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.magic.Magic;
import neon.systems.magic.RSpell;

public class MagicState extends State {
	private static final Logger logger = Logger.getGlobal();

	private final UserInterface ui;
	private final EventBus bus;
	private final ComponentManager components;
	private final ResourceManager resources;

	@FXML private ListView<RSpell> spellList;
	
	private Scene scene;

	public MagicState(UserInterface ui, EventBus bus, ComponentManager components, ResourceManager resources) {
		this.ui = ui;
		this.bus = bus;
		this.components = components;
		this.resources = resources;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Magic.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load magic interface: " + e.getMessage());
		}

		spellList.setOnKeyPressed(event -> keyPressed(event));
		spellList.setCellFactory(spellList -> new SpellCell());
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering magic state");
		bus.register(this);
		
		for (String id : components.getComponent(0, Magic.class).getSpells()) {
			try {
				spellList.getItems().add(resources.getResource("spells", id));
			} catch (ResourceException e) {
				logger.warning("could not find spell <" + id + ">");
			}
		}
		
		ui.showScene(scene);
	}

	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.F2)) {
			showHelp();
		}
	}
	
	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting magic state");
		bus.unregister(this);
	}
	
	@FXML private void showHelp() {}
	@FXML private void equipSpell() {}
	
	private class SpellCell extends ListCell<RSpell> {
		@Override
		public void updateItem(RSpell spell, boolean empty) {
			super.updateItem(spell, empty);
			if (empty || spell == null) {
				setText(null);
			} else {
				setText(spell.id);
			}
		}
	}
}
