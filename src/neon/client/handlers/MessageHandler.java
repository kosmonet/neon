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

import com.google.common.eventbus.Subscribe;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import neon.client.ComponentManager;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Stats;
import neon.common.event.StealthEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.ResourceException;
import neon.systems.combat.CombatEvent;
import neon.systems.time.RestEvent;

/**
 * Shows messages in response to certain events that are received from the 
 * server.
 * 
 * @author mdriesen
 *
 */
public class MessageHandler {
	private static final long PLAYER_UID = 0;
	
	private final UserInterface ui;
	private final ComponentManager components;
	
	public MessageHandler(UserInterface ui, ComponentManager components) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.components = Objects.requireNonNull(components, "component manager");
	}
	
	@Subscribe
	private void onCombat(CombatEvent.Result event) {
		Stats stats = components.getComponent(event.defender, Stats.class);
		String message = "Defender is hit for " + event.damage + " points (" + stats.getHealth() + "/" + stats.getBaseHealth() + ").";
		ui.showOverlayMessage(message, 1000);
	}
	
	@Subscribe
	private void onCombat(CombatEvent.Dodge event) {
		String message = "The attack missed.";
		ui.showOverlayMessage(message, 1000);
	}
	
	@Subscribe
	private void onCombat(CombatEvent.Block event) {
		String message = "The defender block the attack.";
		ui.showOverlayMessage(message, 1000);
	}
	
	@Subscribe
	private void onPickpocketFail(StealthEvent.Empty event) {
		ui.showOverlayMessage("Victim has no possessions.", 1000);
	}
	
	@Subscribe
	private void onPickpocketSuccess(StealthEvent.Stolen event) {
		ui.showOverlayMessage("You've stolen something.", 1000);
	}
	
	@Subscribe
	private void onLockpickSuccess(StealthEvent.Unlocked event) {
		ui.showOverlayMessage("You've picked a lock.", 1000);
	}
	
	@Subscribe
	private void onSkillIncrease(UpdateEvent.Skills event) throws ResourceException {
		if (event.uid == PLAYER_UID) {
			ui.showOverlayMessage(event.skill + " skill increased to " + event.value + ".", 1000);
		}
	}
	
	@Subscribe
	private void onLevelIncrease(UpdateEvent.Level event) throws ResourceException {
		ui.showOverlayMessage("Level up!", 1000);
	}
	
	@Subscribe
	private void onSleep(RestEvent.Wake event) {
		FadeTransition transition = new FadeTransition(Duration.millis(2000), ui.getCurrentScene().getRoot());
		transition.setFromValue(1.0);
	    transition.setToValue(0.0);
	    transition.setAutoReverse(true);
	    transition.setCycleCount(2);
	    transition.setOnFinished(action -> ui.showOverlayMessage("You have slept.", 1500));
	    transition.play();
	}
}
