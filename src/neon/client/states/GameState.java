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
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import neon.client.ComponentManager;
import neon.client.help.HelpWindow;
import neon.client.ui.ButtonTypes;
import neon.client.ui.ClientRenderer;
import neon.client.ui.UserInterface;
import neon.common.entity.PlayerMode;
import neon.common.entity.components.Behavior;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
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
import neon.util.Direction;

/**
 * A module that implements the main game screen.
 * 
 * @author mdriesen
 *
 */
public class GameState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	private final UserInterface ui;
	private final EventBus bus;
	private final RenderPane<Long> renderPane;
	private final ResourceManager resources;
	private final ComponentManager components;
	
	@FXML private StackPane stack;
	@FXML private BorderPane infoPane;
	@FXML private Label modeLabel, healthLabel, manaLabel;
	
	private Scene scene;
	private int scale = 20;
	private RMap map;
	private boolean paused = true;
	private boolean casting = false;
	private long pointer;
	
	/**
	 * Initializes a new game module.
	 * 
	 * @param ui
	 * @param bus
	 * @param provider
	 * @param resources
	 */
	public GameState(UserInterface ui, EventBus bus, ComponentManager components, ResourceManager resources) {
		this.ui = ui;
		this.bus = bus;
		this.resources = resources;
		this.components = components;
		renderPane = new RenderPane<Long>(resources, new ClientRenderer(components));
		
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

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I), () -> bus.post(new TransitionEvent("inventory", map)));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.J), () -> bus.post(new TransitionEvent("journal")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> bus.post(new TransitionEvent("map", map)));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> new HelpWindow().show("game.html"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P), () -> pause());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> quit());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), () -> act());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.K), () -> changeMode());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.C), () -> cast());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S), () -> bus.post(new TransitionEvent("magic")));
	}
	
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) throws ResourceException {
		// prepare the scene
		stack.getChildren().clear();
		renderPane.widthProperty().addListener((observable, oldWidth, newWidth) -> redraw());
		renderPane.heightProperty().addListener((observable, oldHeight, newHeight) -> redraw());
		stack.getChildren().add(renderPane);
		stack.getChildren().add(infoPane);
	}
	
	@Subscribe
	private void onMapChange(UpdateEvent.Map event) throws ResourceException {
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		map = resources.getResource("maps", event.map);
		map.addEntity(PLAYER_UID, shape.getX(), shape.getY());
		renderPane.setMap(map.getTerrain(), map.getElevation(), map.getEntities());		
		redraw();
	}
	
	@Subscribe
	private void onMove(UpdateEvent.Move event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		Platform.runLater(() -> redraw());
	}
	
	@Subscribe
	private void onRemove(UpdateEvent.Remove event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		Platform.runLater(() -> redraw());
	}
	
	private void changeMode() {
		PlayerInfo record = components.getComponent(PLAYER_UID, PlayerInfo.class);
		switch (record.getMode()) {
		case NONE:
			record.setMode(PlayerMode.AGGRESSION);
			break;
		case AGGRESSION:
			record.setMode(PlayerMode.STEALTH);
			break;
		case STEALTH:
			record.setMode(PlayerMode.NONE);
			break;
		}
		modeLabel.setText(record.getMode().toString());		
	}
	
	private void move(Direction direction) {
		if (!casting) {
			bus.post(new InputEvent.Move(direction, map.id));
		} else {
			Shape shape = components.getComponent(pointer, Shape.class);
			switch (direction) {
			case LEFT: 
				shape.setX(Math.max(0, shape.getX() - 1)); 
				break;
			case RIGHT: 
				shape.setX(Math.min(map.getWidth(), shape.getX() + 1)); 
				break;
			case UP: 
				shape.setY(Math.max(0, shape.getY() - 1)); 
				break;
			case DOWN: 
				shape.setY(Math.min(map.getHeight(), shape.getY() + 1)); 
				break;
			}
			redraw();
		}
	}
	
	private void cast() {
		if (!casting) {
			System.out.println("braobel");
			pointer = components.getFreeUID();
			Shape player = components.getComponent(0, Shape.class);
			Shape shape = new Shape(pointer, player.getX(), player.getY(), player.getZ());
			Graphics graphics = new Graphics(pointer, "X", Color.WHITE);
			components.putComponent(pointer, shape);
			components.putComponent(pointer, graphics);
			ArrayList<Long> entities = new ArrayList<>(map.getEntities());
			entities.add(pointer);
			renderPane.updateMap(entities);
			redraw();
			casting = true;
		} else {
			components.removeEntity(pointer);
			renderPane.updateMap(map.getEntities());
			redraw();
			casting = false;
		}
	}
	
	private void redraw() {
		PlayerInfo record = components.getComponent(PLAYER_UID, PlayerInfo.class);
		modeLabel.setText(record.getMode().toString());
		Stats stats = components.getComponent(PLAYER_UID, Stats.class);
		
		healthLabel.setText("♥ " + stats.getHealth() + "/" + stats.getBaseHealth());
		if (stats.getHealth()/stats.getBaseHealth() < 0.1) {
			healthLabel.setTextFill(Color.RED);
		} else {
			healthLabel.setTextFill(Color.SILVER);			
		}
		
		manaLabel.setText("✳ " + stats.getMana() + "/" + stats.getBaseMana());
		if (stats.getMana()/stats.getBaseMana() < 0.1) {
			manaLabel.setTextFill(Color.RED);
		} else {
			manaLabel.setTextFill(Color.SILVER);			
		}		
		
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		int xpos = Math.max(0, (int) (shape.getX() - renderPane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (shape.getY() - renderPane.getHeight()/(2*scale)));
		renderPane.draw(xpos, ypos, scale);
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
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);

		if (!map.getEntities(shape.getX(), shape.getY()).isEmpty()) {
			bus.post(new TransitionEvent("pick", map));			
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
		
		long bumper = event.getBumper();
		long bumped = event.getBumped();

		if (bumper == PLAYER_UID) {
			PlayerInfo record = components.getComponent(bumper, PlayerInfo.class);
			Behavior brain = components.getComponent(bumped, Behavior.class);
	    	Graphics graphics = components.getComponent(bumped, Graphics.class);
	    	CreatureInfo resource = components.getComponent(bumped, CreatureInfo.class);
			
			switch (record.getMode()) {
			case NONE:
				if (brain.isFriendly(bumper)) {
					bus.post(new TransitionEvent("talk", graphics, resource));
				} else {
					bus.post(new CombatEvent.Start(bumper, bumped));	
				}
				break;
			case STEALTH:
				if (brain.isFriendly(bumper)) {
					bus.post(new TransitionEvent("talk", graphics, resource));
				} else {
					bus.post(new CombatEvent.Start(bumper, bumped));	
				}
				break;
			case AGGRESSION:
				if (brain.isFriendly(bumper)) {
					Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
							ButtonTypes.talk, ButtonTypes.attack, ButtonTypes.cancel);
					if (result.get().equals(ButtonTypes.talk)) {
						bus.post(new TransitionEvent("talk", graphics, resource));
					} else if (result.get().equals(ButtonTypes.attack)) {
						bus.post(new CombatEvent.Start(bumper, bumped));	
					}
				} else {
					bus.post(new CombatEvent.Start(bumper, bumped));	
				}
				break;
			}
		}

		// unpause if necessary
		if (!paused) {
			bus.post(new NeonEvent.Unpause());
		}
	}
	
	@Subscribe
	private void onCombat(CombatEvent.Result event) {
		Stats stats = components.getComponent(event.defender, Stats.class);
		System.out.println(stats);
		String message = "Defender is hit for " + event.damage + "points (" + stats.getHealth() + "/" + stats.getBaseHealth() + "). ";
		ui.showOverlayMessage(message, 1000);
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
