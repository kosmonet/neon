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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import neon.client.UserInterface;
import neon.client.ui.MapPane;
import neon.common.resources.ResourceProvider;

/**
 * Map viewer.
 * 
 * @author mdriesen
 *
 */
public class MapModule extends Module {
	private static final Logger logger = Logger.getGlobal();

	private final UserInterface ui;
	private final MapPane pane;
	private Scene scene;

	public MapModule(UserInterface ui, EventBus bus, ResourceProvider provider) {
		this.ui = ui;

		pane = new MapPane(provider);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Map.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load map viewer: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> bus.post(new TransitionEvent("cancel")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> bus.post(new TransitionEvent("cancel")));
	}

	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering map module");
		ui.showScene(scene);
		scene.setRoot(pane);
		pane.drawMap(event.getMap());	
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting map module");
	}
}
