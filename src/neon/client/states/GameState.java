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

import neon.client.ComponentManager;
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
import neon.common.resources.RCreature;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.Skill;
import neon.entity.components.Behavior;
import neon.entity.components.Graphics;
import neon.entity.components.Info;
import neon.entity.components.Shape;
import neon.entity.components.Skills;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;
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

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I), () -> showInventory());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.J), () -> showJournal());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> showMap());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> new HelpWindow().show("game.html"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P), () -> pause());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> quit());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), () -> act());
	}
	
	@Subscribe
	private void onGameStart(UpdateEvent.Start event) throws ResourceException {
		// prepare the player
		RCreature species = resources.getResource("creatures", event.id);
		Stats stats = new Stats(PLAYER_UID, species);
		stats.setBaseCha(event.charisma);
		stats.setBaseCon(event.constitution);
		stats.setBaseDex(event.dexterity);
		stats.setBaseInt(event.intelligence);
		stats.setBaseStr(event.strength);
		stats.setBaseWis(event.wisdom);
		Skills skills = new Skills(PLAYER_UID);
		skills.setSkill(Skill.SWIMMING, event.swimming);
		
		components.putComponent(PLAYER_UID, stats);
		components.putComponent(PLAYER_UID, skills);
		components.putComponent(PLAYER_UID, new Creature.Resource(PLAYER_UID, species));
		components.putComponent(PLAYER_UID, new Graphics(PLAYER_UID, "@", species.color));
		components.putComponent(PLAYER_UID, new Shape(PLAYER_UID, event.x, event.y, event.z));
		components.putComponent(PLAYER_UID, new Info(PLAYER_UID, event.name, event.gender));
		
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
	private void onItemChange(UpdateEvent.Item event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		Platform.runLater(() -> redraw());
	}
	
	@Subscribe
	private void onCreatureChange(UpdateEvent.Creature event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		Platform.runLater(() -> redraw());
	}
	
	@Subscribe
	private void update(UpdateEvent.Move event) throws ResourceException {
		Platform.runLater(() -> redraw());
	}
	
	@Subscribe
	private void update(UpdateEvent.Remove event) throws ResourceException {
		Platform.runLater(() -> renderPane.updateMap(map.getEntities()));
		Platform.runLater(() -> redraw());
	}
	
	private void move(Direction direction) {
		bus.post(new InputEvent.Move(direction, map.id));
	}
	
	private void redraw() {
		Info record = components.getComponent(PLAYER_UID, Info.class);
		modeLabel.setText(record.getMode().toString());
		Stats stats = components.getComponent(PLAYER_UID, Stats.class);
		healthLabel.setText("♥ " + stats.getBaseCon()*3);
		manaLabel.setText("✳ " + stats.getBaseInt()*6);
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		int xpos = Math.max(0, (int) (shape.getX() - renderPane.getWidth()/(2*scale)));
		int ypos = Math.max(0, (int) (shape.getY() - renderPane.getHeight()/(2*scale)));
		renderPane.draw(xpos, ypos, scale);
	}
	
	private void showInventory() {
		bus.post(new TransitionEvent("inventory", map));
	}
	
	private void showMap() {
		bus.post(new TransitionEvent("map", map));
	}
	
	private void showJournal() {
		bus.post(new TransitionEvent("journal"));
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
			bus.post(new TransitionEvent("pick", map, shape));			
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
			Info record = components.getComponent(bumper, Info.class);
			Behavior brain = components.getComponent(bumped, Behavior.class);
	    	Graphics graphics = components.getComponent(bumped, Graphics.class);
	    	Creature.Resource resource = components.getComponent(bumped, Creature.Resource.class);
			
			switch (record.getMode()) {
			case NONE:
				if (brain.isFriendly(bumper)) {
					bus.post(new TransitionEvent("talk", graphics, resource));
				} else {
					bus.post(new CombatEvent(bumper, bumped));	
				}
				break;
			case STEALTH:
				if (brain.isFriendly(bumper)) {
					bus.post(new TransitionEvent("talk", graphics, resource));
				} else {
					bus.post(new CombatEvent(bumper, bumped));	
				}
				break;
			case AGGRESSION:
				if (brain.isFriendly(bumper)) {
					Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
							ButtonTypes.talk, ButtonTypes.attack, ButtonTypes.cancel);
					if (result.get().equals(ButtonTypes.talk)) {
						bus.post(new TransitionEvent("talk", graphics, resource));
					} else if (result.get().equals(ButtonTypes.attack)) {
						bus.post(new CombatEvent(bumper, bumped));	
					}
				} else {
					bus.post(new CombatEvent(bumper, bumped));	
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
