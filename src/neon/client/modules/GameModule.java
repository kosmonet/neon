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
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import neon.client.ClientProvider;
import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.client.ui.ClientRenderer;
import neon.common.event.InputEvent;
import neon.common.event.QuitEvent;
import neon.common.event.SaveEvent;
import neon.common.event.ServerEvent;
import neon.common.graphics.RenderPane;
import neon.common.resources.RMap;
import neon.entity.entities.Player;
import neon.entity.events.UpdateEvent;
import neon.util.Direction;

/**
 * 
 * @author mdriesen
 *
 */
public class GameModule extends Module {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ClientProvider provider;
	private final RenderPane pane;
	
	private Scene scene;
	private int scale = 20;
	private RMap map;
	private boolean paused = true;
	
	public GameModule(UserInterface ui, EventBus bus, ClientProvider provider) {
		this.ui = ui;
		this.bus = bus;
		this.provider = provider;
		pane = new RenderPane(provider, new ClientRenderer());
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Game.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load new game: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT), () -> move(Direction.LEFT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT), () -> move(Direction.RIGHT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP), () -> move(Direction.UP));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN), () -> move(Direction.DOWN));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I), () -> showInventory());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> showMap());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P), () -> pause());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> quit());
	}
	
	@Subscribe
	private void update(UpdateEvent.Start event) {
		// prepare the scene
		scene.setRoot(pane);
		scene.widthProperty().addListener((observable, oldWidth, newWidth) -> redraw());
		scene.heightProperty().addListener((observable, oldHeight, newHeight) -> redraw());		
	}
	
	@Subscribe
	private void update(UpdateEvent.Map event) {
		// store all resources and entities
		provider.clear();
		provider.addResources(event.getResources());
		provider.addEntities(event.getEntities());
		
		map = event.getMap();
		
		pane.setMap(event.getTerrain(), event.getElevation(), provider.getEntities());		
		redraw();
	}
	
	@Subscribe
	private void update(UpdateEvent.Entities event) {
		// store all changed entities
		provider.addEntities(event.getEntities());
		pane.updateMap(provider.getEntities());
		redraw();
	}
	
	private void move(Direction direction) {
		bus.post(new InputEvent.Move(direction));
	}
	
	private void redraw() {
		Player player = (Player) provider.getEntity(0);
		int xpos = Math.max(0, (int) (player.shape.getX() - pane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (player.shape.getY() - pane.getHeight()/(2*scale)));
		pane.draw(xpos, ypos, scale);
	}
	
	private void showInventory() {
		bus.post(new TransitionEvent("inventory"));
	}
	
	private void showMap() {
		bus.post(new TransitionEvent("map", map));
	}
	
	private void showHelp() {
		new HelpWindow().show("game.html");
	}
	
	private void pause() {
		if (paused) {
			paused = false;
			bus.post(new ServerEvent.Unpause());
		} else {
			paused = true;
			bus.post(new ServerEvent.Pause());
		}
	}
	
	private void quit() {
		// pause the server
		bus.post(new ServerEvent.Pause());
		paused = true;
		
		// define two buttons as NO to prevent enter defaulting to the yes button
		ButtonType yes = new ButtonType("yes", ButtonData.NO);
		ButtonType no = new ButtonType("no", ButtonData.NO);
		ButtonType cancel = new ButtonType("cancel", ButtonData.CANCEL_CLOSE);
		
		Optional<ButtonType> result = ui.showQuestion("Save current game before quitting?", yes, no, cancel);
		
		if (result.get().equals(yes)) {
			// server takes care of saving
			bus.post(new SaveEvent());	
		} else if (result.get().equals(no)) {
			// server takes care of quitting
		    bus.post(new QuitEvent());
		}
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering game module");
		ui.showScene(scene);
		
		// unpause the server when returning to the game module
		if(!paused) {
			bus.post(new ServerEvent.Unpause());
		}
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting game module");
		// pause the server when leaving the game module
		bus.post(new ServerEvent.Pause());
	}
}
