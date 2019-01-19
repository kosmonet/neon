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
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import neon.client.Configuration;
import neon.client.Map;
import neon.client.ComponentManager;
import neon.client.help.HelpWindow;
import neon.client.ui.ClientRenderer;
import neon.client.ui.Pointer;
import neon.client.ui.UserInterface;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentEvent;
import neon.common.event.InputEvent;
import neon.common.event.UpdateEvent;
import neon.common.graphics.RenderPane;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.magic.Magic;
import neon.systems.magic.MagicEvent;
import neon.systems.magic.RSpell;
import neon.systems.magic.Target;
import neon.systems.time.RestEvent;
import neon.util.Direction;

/**
 * A module that implements the main game screen.
 * 
 * @author mdriesen
 */
public final class GameState extends State {
	private static final Logger LOGGER = Logger.getGlobal();
	private static final long POINTER_UID = 1;
	
	private final UserInterface ui;
	private final EventBus bus;
	private final RenderPane<Long> renderPane;
	private final ResourceManager resources;
	private final ComponentManager components;
	private final Pointer pointer = new Pointer(POINTER_UID);
	private final Configuration config;
	
	@FXML private StackPane stack;
	@FXML private BorderPane infoPane;
	@FXML private Label modeLabel, infoLabel;
	@FXML private Label healthLabel, manaLabel, staminaLabel;
	
	private Scene scene;
	private int scale = 20;
	private boolean looking = false;
	private boolean redraw = true;
	
	/**
	 * Initializes a new game module. The user interface, event bus, component 
	 * manager, resource manager and configuration must not be null.
	 * 
	 * @param ui
	 * @param bus
	 * @param components
	 * @param resources
	 * @param config
	 */
	public GameState(UserInterface ui, EventBus bus, ComponentManager components, ResourceManager resources, Configuration config) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		this.components = Objects.requireNonNull(components, "component manager");
		this.config = Objects.requireNonNull(config, "configuration");
		
		renderPane = new RenderPane<Long>(resources, new ClientRenderer(components));
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Game.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.setFill(Color.BLACK);
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			LOGGER.severe("failed to load new game: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT), () -> move(Direction.LEFT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT), () -> move(Direction.RIGHT));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP), () -> move(Direction.UP));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN), () -> move(Direction.DOWN));

		// TODO: hotkeys voorzien voor spells of items
		Accelerator accelerator = new Accelerator(ui, bus, components, config);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I), () -> bus.post(new TransitionEvent("inventory")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.J), () -> bus.post(new TransitionEvent("journal")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> bus.post(new TransitionEvent("map")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> new HelpWindow().show("game.html"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P), this::pause);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), accelerator::quit);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), accelerator::act);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.K), () -> accelerator.changeMode(modeLabel));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.C), this::cast);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.L), this::look);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.U), accelerator::use);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R), () -> bus.post(new RestEvent.Sleep()));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S), () -> bus.post(new TransitionEvent("magic")));
		
		scene.setOnMouseClicked(this::click);
		
		components.putComponent(pointer.getShape());
		components.putComponent(pointer.getGraphics());
	}
	
	/**
	 * Handles mouse events.
	 * 
	 * @param event
	 */
	private void click(MouseEvent event) {
		Shape shape = components.getComponent(Configuration.PLAYER_UID, Shape.class);
		int xpos = Math.max(0, (int) (shape.getX() - renderPane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (shape.getY() - renderPane.getHeight()/(2*scale)));
		int x = xpos + (int)(event.getSceneX()/scale);
		int y = ypos + (int)(event.getSceneY()/scale);

		try {
			RTerrain terrain = resources.getResource("terrain", config.getCurrentMap().getTerrain().get(x, y));
			ui.showOverlayMessage(x + "," + y + ": " + terrain.id, 1500);
		} catch (ResourceException e) {
			LOGGER.warning("unknown terrain type: " + config.getCurrentMap().getTerrain().get(x, y));
		}
	}
	
	/**
	 * Handles start events.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) {
		// prepare the scene
		stack.getChildren().clear();
		renderPane.widthProperty().addListener((observable, oldWidth, newWidth) -> redraw());
		renderPane.heightProperty().addListener((observable, oldHeight, newHeight) -> redraw());
		stack.getChildren().add(renderPane);
		stack.getChildren().add(infoPane);
	}
	
	/**
	 * Handles map changes.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onMapChange(UpdateEvent.Map event) {
		renderPane.setMap(config.getCurrentMap());
	}
	
	/**
	 * Handles component updates.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onUpdate(ComponentEvent event) {
		scheduleRedraw();
	}
	
	/**
	 * Handles update events.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onUpdate(UpdateEvent event) {
		scheduleRedraw();
	}
	
	/**
	 * Schedules a redraw of the map.
	 */
	private void scheduleRedraw() {
		if (!redraw) {
			Platform.runLater(this::redraw);
			redraw = true;
		}		
	}

	/**
	 * Moves the player in the given direction.
	 * 
	 * @param direction
	 */
	private void move(Direction direction) {
		Map map = config.getCurrentMap();
		
		if (!looking) {
			bus.post(new InputEvent.Move(direction, map.getId()));
		} else {
			pointer.move(direction, map);
			StringBuilder builder = new StringBuilder();
			Shape position = pointer.getShape();
			
			try {
				RTerrain terrain = resources.getResource("terrain", map.getTerrain().get(position.getX(), position.getY()));
				builder.append(terrain.id);
			} catch (ResourceException e) {
				LOGGER.warning("unknown terrain type: " + map.getTerrain().get(position.getX(), position.getY()));
			}

			for (Long uid : map.getEntities(position.getX(), position.getY())) {
				builder.append(" - ");
				if (components.hasComponent(uid, PlayerInfo.class)) {
					builder.append(components.getComponent(uid, PlayerInfo.class).getName());
				} else if (components.hasComponent(uid, CreatureInfo.class)) {
					builder.append(components.getComponent(uid, CreatureInfo.class).getName());
				} else if (components.hasComponent(uid, DoorInfo.class)) {
					if (components.getComponent(uid, DoorInfo.class).getText().isEmpty()) {
						builder.append(components.getComponent(uid, ItemInfo.class).name);
					} else {
						builder.append(components.getComponent(uid, DoorInfo.class).getText());						
					}
				} else if (components.hasComponent(uid, ItemInfo.class)) {
					builder.append(components.getComponent(uid, ItemInfo.class).name);
				}
			}

			infoLabel.setText(builder.toString());
			redraw();
		}
	}

	/**
	 * Lets the player cast a spell.
	 */
	private void cast() {
		Magic magic = components.getComponent(Configuration.PLAYER_UID, Magic.class);
		if (magic.getEquipped().isPresent()) {
			String id = magic.getEquipped().get();
			
			try {
				RSpell spell = resources.getResource("spells", id);
				
				if (looking && spell.target == Target.OTHER) {
					if (magic.getEquipped().isPresent()) {
						Shape position = pointer.getShape();
						Optional<Long> creature = config.getCurrentMap().getEntities(position.getX(), position.getY()).stream()
								.filter(uid -> components.hasComponent(uid, CreatureInfo.class)).findFirst();
						if (creature.isPresent() && creature.get() != Configuration.PLAYER_UID) {
							creature.ifPresent(uid -> bus.post(new MagicEvent.Cast(Configuration.PLAYER_UID, magic.getEquipped().get(), uid)));
						}
						look();
					}
				} else if (spell.target == Target.SELF) {
					bus.post(new MagicEvent.Cast(Configuration.PLAYER_UID, magic.getEquipped().get(), Configuration.PLAYER_UID));
				} else {
					look();
				}
			} catch (ResourceException e) {
				LOGGER.severe("spell <" + id + "> not found");
			}
		} 
	}

	/**
	 * Lets the player look around by pausing the map and transferring control
	 * to a pointer.
	 */
	private void look() {
		if (!looking) {
			Shape position = components.getComponent(Configuration.PLAYER_UID, Shape.class);
			pointer.getShape().setPosition(position.getX(), position.getY(), position.getZ());
			config.getCurrentMap().addEntity(POINTER_UID, position.getX(), position.getY());
			infoLabel.setVisible(true);
			redraw();
			looking = true;
		} else {
			config.getCurrentMap().removeEntity(POINTER_UID);
			infoLabel.setVisible(false);
			redraw();
			looking = false;
		}
	}
	
	/**
	 * Redraws the user interface.
	 */
	private void redraw() {
		PlayerInfo record = components.getComponent(Configuration.PLAYER_UID, PlayerInfo.class);
		modeLabel.setText(record.getMode().toString());
		Stats stats = components.getComponent(Configuration.PLAYER_UID, Stats.class);
		
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
		
		Shape shape = components.getComponent(Configuration.PLAYER_UID, Shape.class);
		int xpos = Math.max(0, (int) (shape.getX() - renderPane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (shape.getY() - renderPane.getHeight()/(2*scale)));
		renderPane.draw(xpos, ypos, scale);
		redraw = false;
	}
	
	/**
	 * (Un)pauses the game.
	 */
	private void pause() {
		if (config.isPaused()) {
			config.unpause();
			bus.post(new InputEvent.Unpause());
		} else {
			config.pause();
			bus.post(new InputEvent.Pause());
		}
	}

	@Override
	public void enter(TransitionEvent event) {
		LOGGER.finest("entering game module");
		ui.showScene(scene);
		
		// unpause the server when returning to the game module
		if (!config.isPaused()) {
			bus.post(new InputEvent.Unpause());
		}
	}

	@Override
	public void exit(TransitionEvent event) {
		LOGGER.finest("exiting game module");
		// pause the server when leaving the game module
		bus.post(new InputEvent.Pause());
	}
}
