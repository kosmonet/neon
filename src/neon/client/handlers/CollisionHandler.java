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

import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.scene.control.ButtonType;
import neon.client.ComponentManager;
import neon.client.states.TransitionEvent;
import neon.client.ui.ButtonTypes;
import neon.client.ui.UserInterface;
import neon.common.entity.components.PlayerInfo;
import neon.common.event.CollisionEvent;
import neon.common.event.InputEvent;
import neon.common.event.StealthEvent;
import neon.systems.ai.Behavior;
import neon.systems.combat.CombatEvent;

public class CollisionHandler {
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final UserInterface ui;
	private final ComponentManager components;
	
	private boolean paused = true;
	
	public CollisionHandler(UserInterface ui, EventBus bus, ComponentManager components) {
		this.ui = ui;
		this.bus = bus;
		this.components = components;
	}
	
	@Subscribe 
	private void onPause(InputEvent.Pause event) {
		paused = true;
	}
	
	@Subscribe 
	private void onUnpause(InputEvent.Unpause event) {
		paused = false;
	}
	
	@Subscribe
	private void onCollision(CollisionEvent event) {
		long bumper = event.bumper;
		long bumped = event.bumped;

		if (bumper == PLAYER_UID) {
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
							ButtonTypes.talk, ButtonTypes.pick, ButtonTypes.cancel);
					if (result.get().equals(ButtonTypes.talk)) {
						bus.post(new TransitionEvent("talk", bumped));
					} else if (result.get().equals(ButtonTypes.pick)) {
						bus.post(new StealthEvent.Pick(bumped));
					}
				} else {
					Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
							ButtonTypes.pick, ButtonTypes.attack, ButtonTypes.cancel);
					if (result.get().equals(ButtonTypes.pick)) {
						bus.post(new StealthEvent.Pick(bumped));
					} else if (result.get().equals(ButtonTypes.attack)) {
						bus.post(new CombatEvent.Start(bumper, bumped));	
					}
				}
				break;
			case AGGRESSION:
				if (brain.isFriendly(bumper)) {
					Optional<ButtonType> result = ui.showQuestion("What do you want to do?", 
							ButtonTypes.talk, ButtonTypes.attack, ButtonTypes.cancel);
					if (result.get().equals(ButtonTypes.talk)) {
						bus.post(new TransitionEvent("talk", bumped));
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
			bus.post(new InputEvent.Unpause());
		}
	}
}
