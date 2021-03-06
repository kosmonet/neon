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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import neon.client.ui.UserInterface;

/**
 * A state for trading.
 * 
 * @author mdriesen
 *
 */
public class TradeState extends State {
	private static final Logger LOGGER = Logger.getGlobal();

	@FXML private Button cancelButton;
	@FXML private ListView<Long> playerList, containerList;

	private final UserInterface ui;
	private final EventBus bus;
	private Scene scene;

	/**
	 * The user interface and event bus must not be null.
	 * 
	 * @param ui
	 * @param bus
	 */
	public TradeState(UserInterface ui, EventBus bus) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Trade.fxml"));
		loader.setController(this);

		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			LOGGER.severe("failed to load trade state: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		
		// list catches the esc key, we need a separate listener
		playerList.setOnKeyPressed(this::keyPressed);
		containerList.setOnKeyPressed(this::keyPressed);
	}
	
	/**
	 * Handles key presses.
	 * 
	 * @param event
	 */
	private void keyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.F1)) {
			showHelp();
		}
	}
	
	/**
	 * Trades an item.
	 */
	@FXML private void buy() {
		
	}
	
	/**
	 * Shows the help window.
	 */
	@FXML private void showHelp() {
		
	}
	
	@Override
	public void enter(TransitionEvent event) {
		LOGGER.finest("entering trade state");		
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		LOGGER.finest("exiting trade state");
	}
}
