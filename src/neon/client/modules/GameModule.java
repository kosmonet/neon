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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import neon.client.ClientProvider;
import neon.client.UserInterface;
import neon.client.ui.ClientRenderer;
import neon.common.event.InputEvent;
import neon.common.graphics.RenderPane;
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
	private final ClientProvider provider = new ClientProvider();
	private final RenderPane pane = new RenderPane(provider, new ClientRenderer());
	
	private Scene scene;
	private int scale = 20;
	
	public GameModule(UserInterface ui, EventBus bus) {
		this.ui = ui;
		this.bus = bus;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/Game.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load new game: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT), () -> move(Direction.LEFT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT), () -> move(Direction.RIGHT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP), () -> move(Direction.UP));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN), () -> move(Direction.DOWN));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I), () -> showInventory());
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
