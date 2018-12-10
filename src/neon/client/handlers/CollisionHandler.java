/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

package neon.client.handlers;

import java.util.Objects;
import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.scene.control.ButtonType;
import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.states.TransitionEvent;
import neon.client.ui.ButtonTypes;
import neon.client.ui.UserInterface;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.PlayerInfo;
import neon.common.event.CollisionEvent;
import neon.common.event.DoorEvent;
import neon.common.event.InputEvent;
import neon.common.event.StealthEvent;
import neon.systems.ai.Behavior;
import neon.systems.combat.CombatEvent;

/**
 * Handles all collisions on the map.
 * 
 * @author mdriesen
 *
 */
public final class CollisionHandler {
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final UserInterface ui;
	private final ComponentManager components;
	private final Configuration config;
	
	public CollisionHandler(UserInterface ui, EventBus bus, ComponentManager components, Configuration config) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.components = Objects.requireNonNull(components, "component manager");
		this.config = Objects.requireNonNull(config, "configuration");
	}

	/**
	 * Handles a collision.
	 * 
	 * @param event	a {@code CollisionEvent} describing the collision
	 */
	@Subscribe
	private void onCollision(CollisionEvent event) {
		long bumper = event.bumper;
		long bumped = event.bumped;

		if (bumper == PLAYER_UID) {
			if (components.hasComponent(bumped, DoorInfo.class)) {
				handleDoor(bumper, bumped);
			} else if (components.hasComponent(bumped, CreatureInfo.class)){
				handleCreature(bumper, bumped);
			}
		}

		// unpause if necessary
		if (!config.isPaused()) {
			bus.post(new InputEvent.Unpause());
		}
	}
	
	/**
	 * Handles the player bumping into a closed door.
	 * 
	 * @param player
	 * @param door
	 */
	private void handleDoor(long player, long door) {
		Optional<ButtonType> result = ui.showQuestion("Open door?", ButtonTypes.YES, ButtonTypes.NO);
		if (result.orElse(ButtonTypes.NO).equals(ButtonTypes.YES)) {
			bus.post(new DoorEvent.Open(door));
		}
	}

	/**
	 * Handles the player bumping into another creature.
	 * 
	 * @param bumper
	 * @param bumped
	 */
	private void handleCreature(long bumper, long bumped) {
		PlayerInfo player = components.getComponent(bumper, PlayerInfo.class);
		Behavior brain = components.getComponent(bumped, Behavior.class);

		switch (player.getMode()) {
		case NONE:
			if (brain.isFriendly(bumper)) {
				bus.post(new TransitionEvent("talk", bumped));
			} else {
				bus.post(new CombatEvent.Start(bumper, bumped));	
			}
			break;
		case STEALTH:
			if (brain.isFriendly(bumper)) {
				Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
						ButtonTypes.TALK, ButtonTypes.PICK, ButtonTypes.CANCEL);
				if (result.get().equals(ButtonTypes.TALK)) {
					bus.post(new TransitionEvent("talk", bumped));
				} else if (result.get().equals(ButtonTypes.PICK)) {
					bus.post(new StealthEvent.Pick(bumped));
				}
			} else {
				Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
						ButtonTypes.PICK, ButtonTypes.ATTACK, ButtonTypes.CANCEL);
				if (result.get().equals(ButtonTypes.PICK)) {
					bus.post(new StealthEvent.Pick(bumped));
				} else if (result.get().equals(ButtonTypes.ATTACK)) {
					bus.post(new CombatEvent.Start(bumper, bumped));	
				}
			}
			break;
		case AGGRESSION:
			if (brain.isFriendly(bumper)) {
				Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
						ButtonTypes.TALK, ButtonTypes.ATTACK, ButtonTypes.CANCEL);
				if (result.get().equals(ButtonTypes.TALK)) {
					bus.post(new TransitionEvent("talk", bumped));
				} else if (result.get().equals(ButtonTypes.ATTACK)) {
					bus.post(new CombatEvent.Start(bumper, bumped));	
				}
			} else {
				bus.post(new CombatEvent.Start(bumper, bumped));	
			}
			break;
		}
	}
}
