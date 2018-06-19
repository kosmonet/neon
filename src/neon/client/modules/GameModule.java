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

package neon.client.modules;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import neon.client.ClientProvider;
import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.client.ui.ButtonTypes;
import neon.client.ui.ClientRenderer;
import neon.common.event.CollisionEvent;
import neon.common.event.CombatEvent;
import neon.common.event.InputEvent;
import neon.common.event.NeonEvent;
import neon.common.event.QuitEvent;
import neon.common.event.SaveEvent;
import neon.common.event.UpdateEvent;
import neon.common.graphics.RenderPane;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.components.Behavior;
import neon.entity.components.Info;
import neon.entity.components.Shape;
import neon.entity.entities.Creature;
import neon.entity.entities.Item;
import neon.entity.entities.Player;
import neon.util.Direction;

/**
 * A module that implements the main game screen.
 * 
 * @author mdriesen
 *
 */
public class GameModule extends Module {
	private static final Logger logger = Logger.getGlobal();
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ClientProvider provider;
	private final RenderPane renderPane;
	private final ResourceManager resources;
	
	@FXML private StackPane stack;
	@FXML private BorderPane infoPane;
	@FXML private Label modeLabel;
	
	private Scene scene;
	private int scale = 20;
	private RMap map;
	private boolean paused = true;
	
	/**
	 * Initializes a new game module.
	 * 
	 * @param ui
	 * @param bus
	 * @param provider
	 * @param resources
	 */
	public GameModule(UserInterface ui, EventBus bus, ClientProvider provider, ResourceManager resources) {
		this.ui = ui;
		this.bus = bus;
		this.provider = provider;
		this.resources = resources;
		renderPane = new RenderPane(resources, new ClientRenderer());
		
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
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), () -> act());
	}
	
	@Subscribe
	private void update(UpdateEvent.Start event) throws ResourceException {
		// prepare the player
		Player player = new Player("", "", resources.getResource("creatures", event.id));
		player.getComponent(Shape.class).setPosition(event.x, event.y, event.z);
		provider.addEntity(player);
		
		// prepare the scene
		stack.getChildren().clear();
		renderPane.widthProperty().addListener((observable, oldWidth, newWidth) -> redraw());
		renderPane.heightProperty().addListener((observable, oldHeight, newHeight) -> redraw());
		stack.getChildren().add(renderPane);
		stack.getChildren().add(infoPane);
	}
	
	@Subscribe
	private void update(UpdateEvent.Map event) throws ResourceException {
		map = resources.getResource("maps", event.getMap());
		renderPane.setMap(map.getTerrain(), map.getElevation(), provider.getEntities());		
		redraw();
	}
	
	@Subscribe
	private void update(UpdateEvent.Item event) throws ResourceException {
		if(provider.hasEntity(event.uid)) {
			if (!event.map.isEmpty()) {
				Item item = provider.getEntity(event.uid);
				item.getComponent(Shape.class).setPosition(event.x, event.y, event.z);
				map.moveEntity(item.uid, event.x, event.y);
			} else {
				return;
			}
		} else {
			Item item = new Item(event.uid, resources.getResource("items", event.id));
			provider.addEntity(item);
			if (!event.map.isEmpty()) {
				item.getComponent(Shape.class).setPosition(event.x, event.y, event.z);
				map.addEntity(item.uid, event.x, event.y);
			} else {
				return;
			}
		}

		renderPane.updateMap(provider.getEntities());
		redraw();
	}
	
	@Subscribe
	private void update(UpdateEvent.Creature event) throws ResourceException {
		if(provider.hasEntity(event.uid)) {
			Creature creature = provider.getEntity(event.uid);
			creature.getComponent(Shape.class).setPosition(event.x, event.y, event.z);
			map.moveEntity(event.uid, event.x, event.y);
		} else {
			Creature creature = new Creature(event.uid, resources.getResource("creatures", event.id));
			creature.getComponent(Shape.class).setPosition(event.x, event.y, event.z);
			map.addEntity(event.uid, event.x, event.y);
			provider.addEntity(creature);
		}

		renderPane.updateMap(provider.getEntities());
		redraw();
	}
	
	@Subscribe
	private void update(UpdateEvent.Move event) throws ResourceException {
		if (event.uid == 0) {
			Creature creature = provider.getEntity(event.uid);
			creature.getComponent(Shape.class).setPosition(event.x, event.y, event.z);			
		} else if (provider.hasEntity(event.uid)) {
			Creature creature = provider.getEntity(event.uid);
			creature.getComponent(Shape.class).setPosition(event.x, event.y, event.z);
			map.moveEntity(event.uid, event.x, event.y);
		} 
		redraw();
	}
	
	private void move(Direction direction) {
		bus.post(new InputEvent.Move(direction, map.id));
	}
	
	private void redraw() {
		Player player = provider.getEntity(0);
		Info record = player.getComponent(Info.class);
		modeLabel.setText(record.getMode().toString());
		Shape shape = player.getComponent(Shape.class);
		int xpos = Math.max(0, (int) (shape.getX() - renderPane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (shape.getY() - renderPane.getHeight()/(2*scale)));
		renderPane.draw(xpos, ypos, scale);
	}
	
	private void showInventory() {
		bus.post(new TransitionEvent("inventory"));
	}
	
	private void showMap() {
		bus.post(new TransitionEvent("map", "map", map));
	}
	
	private void showHelp() {
		new HelpWindow().show("game.html");
	}
	
	private void pause() {
		if (paused) {
			paused = false;
			bus.post(new NeonEvent.Unpause());
		} else {
			paused = true;
			bus.post(new NeonEvent.Pause());
		}
	}
	
	private void act() {
		Player player = provider.getEntity(0);
		Shape shape = player.getComponent(Shape.class);

		if (!map.getEntities(shape.getX(), shape.getY()).isEmpty()) {
			bus.post(new TransitionEvent("pick", "map", map));			
		}
	}
	
	private void quit() {
		// pause the server
		if (!paused) {
			bus.post(new NeonEvent.Pause());
		}
		
		Optional<ButtonType> result = ui.showQuestion("Save current game before quitting?", 
				ButtonTypes.yes, ButtonTypes.no, ButtonTypes.cancel);

		if (result.get().equals(ButtonTypes.yes)) {
			// server takes care of saving
			bus.post(new SaveEvent());	
		} else if (result.get().equals(ButtonTypes.no)) {
			// server takes care of quitting
		    bus.post(new QuitEvent());
		}
		
		// unpause if necessary
		if (!paused) {
			bus.post(new NeonEvent.Unpause());
		}
	}
	
	@Subscribe
	private void collide(CollisionEvent event) {
		// pause the server
		if (!paused) {
			bus.post(new NeonEvent.Pause());
		}
		
		Creature one = provider.getEntity(event.getBumper());
		Creature two = provider.getEntity(event.getBumped());
		
		if (one instanceof Player) {
			Player player = (Player) one;
			Behavior brain = two.getComponent(Behavior.class);
			Info record = player.getComponent(Info.class);
			
			switch (record.getMode()) {
			case NONE:
				if (brain.isFriendly(player)) {
					bus.post(new TransitionEvent("talk", "player", player, "creature", two));
				} else {
					bus.post(new CombatEvent(player.uid, two.uid));	
				}
				break;
			case STEALTH:
				if (brain.isFriendly(player)) {
					bus.post(new TransitionEvent("talk", "player", player, "creature", two));
				} else {
					bus.post(new CombatEvent(player.uid, two.uid));	
				}
				break;
			case AGGRESSION:
				if (brain.isFriendly(player)) {
					Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
							ButtonTypes.talk, ButtonTypes.attack, ButtonTypes.cancel);
					if (result.get().equals(ButtonTypes.talk)) {
						bus.post(new TransitionEvent("talk", "player", player, "creature", two));
					} else if (result.get().equals(ButtonTypes.attack)) {
						bus.post(new CombatEvent(player.uid, two.uid));	
					}
				} else {
					bus.post(new CombatEvent(player.uid, two.uid));	
				}
				break;
			}
		}

		// unpause if necessary
		if (!paused) {
			bus.post(new NeonEvent.Unpause());
		}
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering game module");
		ui.showScene(scene);
		
		// unpause the server when returning to the game module
		if (!paused) {
			bus.post(new NeonEvent.Unpause());
		}
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting game module");
		// pause the server when leaving the game module
		bus.post(new NeonEvent.Pause());
	}
}
