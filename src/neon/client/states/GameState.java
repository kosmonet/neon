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
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.animation.FadeTransition;
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
import javafx.util.Duration;
import neon.client.ComponentManager;
import neon.client.help.HelpWindow;
import neon.client.ui.ButtonTypes;
import neon.client.ui.ClientRenderer;
import neon.client.ui.Pointer;
import neon.client.ui.UserInterface;
import neon.common.entity.PlayerMode;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.InputEvent;
import neon.common.event.UpdateEvent;
import neon.common.graphics.RenderPane;
import neon.common.resources.RMap;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.magic.Enchantment;
import neon.systems.magic.Magic;
import neon.systems.magic.MagicEvent;
import neon.systems.time.RestEvent;
import neon.util.Direction;

/**
 * A module that implements the main game screen.
 * 
 * @author mdriesen
 *
 */
public final class GameState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	private static final long POINTER_UID = 1;
	
	private final UserInterface ui;
	private final EventBus bus;
	private final RenderPane<Long> renderPane;
	private final ResourceManager resources;
	private final ComponentManager components;
	private final Pointer pointer = new Pointer(POINTER_UID);
	
	@FXML private StackPane stack;
	@FXML private BorderPane infoPane;
	@FXML private Label modeLabel, infoLabel;
	@FXML private Label healthLabel, manaLabel, staminaLabel;
	
	private Scene scene;
	private int scale = 20;
	private RMap map;
	private boolean paused = true;
	private boolean looking = false;
	private boolean redraw = true;
	
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
			scene.setFill(Color.BLACK);
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load new game: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT), () -> move(Direction.LEFT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT), () -> move(Direction.RIGHT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP), () -> move(Direction.UP));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN), () -> move(Direction.DOWN));

		// TODO: hotkeys voorzien voor spells of items
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I), () -> bus.post(new TransitionEvent("inventory", map)));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.J), () -> bus.post(new TransitionEvent("journal")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> bus.post(new TransitionEvent("map", map)));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> new HelpWindow().show("game.html"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P), () -> pause());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> quit());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), () -> act());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.K), () -> changeMode());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.C), () -> cast());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.L), () -> look());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.U), () -> use());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R), () -> bus.post(new RestEvent.Sleep()));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S), () -> bus.post(new TransitionEvent("magic")));
		
		components.putComponent(pointer.getShape());
		components.putComponent(pointer.getGraphics());
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
		scheduleRedraw();
	}
	
	private void scheduleRedraw() {
		if (!redraw) {
			Platform.runLater(() -> redraw());
			redraw = true;
		}		
	}
	
	@Subscribe
	private void onMove(UpdateEvent.Move event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		scheduleRedraw();
	}
	
	@Subscribe
	private void onRemove(UpdateEvent.Remove event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		scheduleRedraw();
	}
	
	@Subscribe
	private void onUpdate(ComponentUpdateEvent event) {
		scheduleRedraw();
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
		if (!looking) {
			bus.post(new InputEvent.Move(direction, map.id));
		} else {
			pointer.move(direction, map);
			StringBuilder builder = new StringBuilder();
			
			try {
				RTerrain terrain = resources.getResource("terrain", map.getTerrain().get(pointer.getX(), pointer.getY()));
				builder.append(terrain.id);
			} catch (ResourceException e) {
				logger.warning("unknown terrain type: " + map.getTerrain().get(pointer.getX(), pointer.getY()));
			}

			for (Long uid : map.getEntities(pointer.getX(), pointer.getY())) {
				builder.append(" - ");
				if (components.hasComponent(uid, PlayerInfo.class)) {
					builder.append(components.getComponent(uid, PlayerInfo.class).getName());
				} else if (components.hasComponent(uid, CreatureInfo.class)) {
					builder.append(components.getComponent(uid, CreatureInfo.class).getName());
				} else if (components.hasComponent(uid, ItemInfo.class)) {
					builder.append(components.getComponent(uid, ItemInfo.class).name);
				}
			}

			infoLabel.setText(builder.toString());
			redraw();
		}
	}
	
	private void cast() {
		Magic magic = components.getComponent(PLAYER_UID, Magic.class);
		if (magic.getEquiped().isPresent()) {
			bus.post(new MagicEvent.Cast(PLAYER_UID, magic.getEquiped().get(), PLAYER_UID));
		} 
	}
	
	private void use() {
		Inventory inventory = components.getComponent(PLAYER_UID, Inventory.class);
		ArrayList<ButtonType> items = new ArrayList<>();
		HashMap<ButtonType, Long> mapping = new HashMap<>();
		
		for (long item : inventory.getEquippedItems()) {
			if (components.hasComponent(item, Enchantment.class)) {
				ButtonType button = new ButtonType(components.getComponent(item, ItemInfo.class).name);
				mapping.put(button, item);
				items.add(button);
			}
		}
		
		Optional<ButtonType> result = ui.showQuestion("What item to use?", items.toArray(new ButtonType[items.size()]));
		if (result.isPresent()) {
			bus.post(new MagicEvent.Use(mapping.get(result.get())));
		}
	}
	
	private void look() {
		if (!looking) {
			pointer.setPosition(components.getComponent(PLAYER_UID, Shape.class));
			ArrayList<Long> entities = new ArrayList<>(map.getEntities());
			entities.add(POINTER_UID);
			renderPane.updateMap(entities);
			infoLabel.setVisible(true);
			redraw();
			looking = true;
		} else {
			renderPane.updateMap(map.getEntities());
			infoLabel.setVisible(false);
			redraw();
			looking = false;
		}
	}
	
	private void redraw() {
		PlayerInfo record = components.getComponent(PLAYER_UID, PlayerInfo.class);
		modeLabel.setText(record.getMode().toString());
		Stats stats = components.getComponent(PLAYER_UID, Stats.class);
		
		healthLabel.setText("♥ " + stats.getHealth() + "/" + stats.getBaseHealth());
		if ((float) stats.getHealth()/stats.getBaseHealth() < 0.1) {
			healthLabel.setTextFill(Color.RED);
		} else {
			healthLabel.setTextFill(Color.SILVER);			
		}
		
		manaLabel.setText("✳ " + stats.getMana() + "/" + stats.getBaseMana());
		if ((float) stats.getMana()/stats.getBaseMana() < 0.1) {
			manaLabel.setTextFill(Color.RED);
		} else {
			manaLabel.setTextFill(Color.SILVER);			
		}		
		
		staminaLabel.setText("♉ " + stats.getStamina() + "/" + stats.getBaseStamina());
		if ((float) stats.getStamina()/stats.getBaseStamina() < 0.1) {
			staminaLabel.setTextFill(Color.RED);
		} else {
			staminaLabel.setTextFill(Color.SILVER);			
		}		
		
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		int xpos = Math.max(0, (int) (shape.getX() - renderPane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (shape.getY() - renderPane.getHeight()/(2*scale)));
		renderPane.draw(xpos, ypos, scale);
		redraw = false;
	}
	
	private void pause() {
		if (paused) {
			paused = false;
			bus.post(new InputEvent.Unpause());
		} else {
			paused = true;
			bus.post(new InputEvent.Pause());
		}
	}
	
	private void act() {
		// check if there's another entity besides the player on the given position
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		if (map.getEntities(shape.getX(), shape.getY()).size() > 1) {
			bus.post(new TransitionEvent("pick", map));			
		}
	}
	
	@Subscribe
	private void onSleep(RestEvent.Wake event) {
		FadeTransition transition = new FadeTransition(Duration.millis(2000), stack);
		transition.setFromValue(1.0);
	    transition.setToValue(0.0);
	    transition.setAutoReverse(true);
	    transition.setCycleCount(2);
	    transition.setOnFinished(action -> ui.showOverlayMessage("You have slept.", 1500));
	    transition.play();
	}
	
	private void quit() {
		// pause the server
		if (!paused) {
			bus.post(new InputEvent.Pause());
		}
		
		Optional<ButtonType> result = ui.showQuestion("Save current game before quitting?", 
				ButtonTypes.yes, ButtonTypes.no, ButtonTypes.cancel);

		if (result.get().equals(ButtonTypes.yes)) {
			// server takes care of saving
			bus.post(new InputEvent.Save());
		    bus.post(new InputEvent.Quit());
		} else if (result.get().equals(ButtonTypes.no)) {
			// server takes care of quitting
		    bus.post(new InputEvent.Quit());
		}
		
		// unpause if necessary
		if (!paused) {
			bus.post(new InputEvent.Unpause());
		}
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering game module");
		ui.showScene(scene);
		
		// unpause the server when returning to the game module
		if (!paused) {
			bus.post(new InputEvent.Unpause());
		}
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting game module");
		// pause the server when leaving the game module
		bus.post(new InputEvent.Pause());
	}
}
