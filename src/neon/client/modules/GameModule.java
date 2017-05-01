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

import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import neon.client.ClientProvider;
import neon.client.UserInterface;
import neon.system.event.UpdateEvent;
import neon.system.graphics.RenderPane;
import neon.system.resources.CGame;
import neon.system.resources.RMap;
import neon.system.resources.ResourceException;

/**
 * 
 * @author mdriesen
 *
 */
public class GameModule extends Module {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private RenderPane pane;	
	private Scene scene;
	private int xpos = 0, ypos = 0;
	
	public GameModule(UserInterface ui) {
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/Game.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load new game menu: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT), () -> move("left"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT), () -> move("right"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP), () -> move("up"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN), () -> move("down"));
		
	}
	
	@Subscribe
	private void start(UpdateEvent.Start event) throws ResourceException {
		// store all resources
		ClientProvider provider = new ClientProvider();
		provider.addAll(event.getResources());
		
		// get the right map
		CGame game = provider.getResource("config", "game");
		RMap map = provider.getResource("maps", game.getStartMap());
		
		// and draw the scene
		pane = new RenderPane(provider);
		scene.setRoot(pane);
		pane.setMap(map.getTerrain(), map.getElevation());
		
		scene.widthProperty().addListener((observable, oldWidth, newWidth) -> pane.draw(xpos, ypos));
		scene.heightProperty().addListener((observable, oldHeight, newHeight) -> pane.draw(xpos, ypos));
		
		pane.draw(xpos, ypos);
	}
	
	private void move(String direction) {
		switch(direction) {
		case "left": xpos = Math.max(0, xpos - 1); break;
		case "right": xpos++; break;
		case "up": ypos = Math.max(0, ypos - 1); break;
		case "down": ypos++; break;
		}

		pane.draw(xpos, ypos);
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering game module");
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting game module");
	}
}
