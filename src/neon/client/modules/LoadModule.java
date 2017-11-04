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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.common.event.ClientLoadEvent;
import neon.common.event.ServerLoadEvent;

/**
 * This module shows the load game screen.
 * 
 * @author mdriesen
 *
 */
public class LoadModule extends Module {
	private static final Logger logger = Logger.getGlobal();

	private final EventBus bus;
	private final UserInterface ui;

	@FXML private Button cancelButton, startButton;
	@FXML private ListView<String> saveList;
	
	private Scene scene;
	
	/**
	 * Initializes this module.
	 * 
	 * @param ui
	 * @param bus	the client event bus
	 */
	public LoadModule(UserInterface ui, EventBus bus) {
		this.bus = bus;
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Load.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("failed to load load game menu: " + e.getMessage());
		}
		
		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		
		// list catches the esc and enter keys, we need a separate listener
		saveList.setOnKeyPressed(event -> keyPressed(event));
	}
	
	@FXML private void startGame() {
		if(saveList.getSelectionModel().getSelectedItem() != null) {
			// let the server know we want to load a saved game
			bus.post(new ServerLoadEvent.Start(saveList.getSelectionModel().getSelectedItem()));
			// transition to the actual game module
			bus.post(new TransitionEvent("start game"));
		}
	}
	
	/**
	 * Shows the saved characters in the list.
	 * 
	 * @param event
	 */
	@Subscribe
	private void list(ClientLoadEvent.List event) {
		saveList.getItems().clear();
		saveList.getItems().addAll(event.getSaves());
		saveList.getSelectionModel().select(0);
	}
	
	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.ENTER)) {
			startGame();
		} else if (event.getCode().equals(KeyCode.F2)) {
			showHelp();
		}
	}
	
	@FXML private void showHelp() {
		new HelpWindow().show("load.html");
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering load game module");
		ui.showScene(scene);
		bus.post(new ServerLoadEvent.List());
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting load game module");		
	}
}
