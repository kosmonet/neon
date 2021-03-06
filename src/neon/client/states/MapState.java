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
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import neon.client.Configuration;
import neon.client.Map;
import neon.client.help.HelpWindow;
import neon.client.ui.MapPane;
import neon.client.ui.UserInterface;
import neon.common.resources.ResourceManager;

/**
 * Map viewer.
 * 
 * @author mdriesen
 *
 */
public final class MapState extends State {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final UserInterface ui;
	private final MapPane pane;
	private final Configuration config;
	
	@FXML private Button cancelButton;
	@FXML private BorderPane root;
	
	private Scene scene;

	public MapState(UserInterface ui, EventBus bus, ResourceManager resources, Configuration config) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.config = Objects.requireNonNull(config, "configuration");
		Objects.requireNonNull(bus, "event bus");
		pane = new MapPane(resources);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Map.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			LOGGER.severe("failed to load map viewer: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> bus.post(new TransitionEvent("cancel")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::showHelp);

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
	}

	@FXML private void showHelp() {
		new HelpWindow().show("map.html");
	}
	
	@Override
	public void enter(TransitionEvent event) {
		LOGGER.finest("entering map state");
	    root.setCenter(pane);
	    Map map = config.getCurrentMap();
	    pane.widthProperty().addListener(observable -> pane.drawMap(map));
	    pane.heightProperty().addListener(observable -> pane.drawMap(map));
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		LOGGER.finest("exiting map state");
	}
}
