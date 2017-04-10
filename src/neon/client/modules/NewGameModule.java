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

package neon.client.modules;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import neon.client.UserInterface;
import neon.system.event.ClientConfigurationEvent;

public class NewGameModule extends Module {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private Button cancelButton, startButton;
	@FXML private ListView<String> speciesList;
	@FXML private ToggleGroup genderGroup;
	@FXML private TextField nameField;
	
	private final UserInterface ui;
	private final EventBus bus;
	private Scene scene;

	public NewGameModule(UserInterface ui, EventBus bus) {
		this.ui = ui;
		this.bus = bus;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/NewGame.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../scenes/main.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("failed to load new game menu");
		}
		
		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		startButton.setOnAction(event -> bus.post(new TransitionEvent("start game")));

		// list catches the esc and enter keys, we need a separate listener
		speciesList.setOnKeyPressed(event -> keyPressed(event));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering new game module");
		ui.showScene(scene);
		nameField.requestFocus();
		
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting new game module");
	}
	
	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.ENTER)) {
			bus.post(new TransitionEvent("start game"));			
		}
	}
	
	/**
	 * Configures the scene of this main menu module.
	 * 
	 * @param event
	 */
	@Subscribe
	public void configure(ClientConfigurationEvent event) {
		speciesList.getItems().addAll(event.getPlayableSpecies());
		speciesList.getSelectionModel().select(0);
	}	
}
