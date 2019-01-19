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
import neon.client.Configuration;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Stats;
import neon.common.event.StealthEvent;
import neon.common.event.UpdateEvent;
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
	private final UserInterface ui;
	private final ComponentManager components;
	
	/**
	 * The user interface and component manager must not be null.
	 * 
	 * @param ui	the {@code UserInterface} of the client
	 * @param components	the client component manager
	 */
	public MessageHandler(UserInterface ui, ComponentManager components) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.components = Objects.requireNonNull(components, "component manager");
	}
	
	/**
	 * Shows the result of combat.
	 * 
	 * @param event	a {@code CombatEvent} describing the result
	 */
	@Subscribe
	private void onCombat(CombatEvent.Result event) {
		Stats stats = components.getComponent(event.defender, Stats.class);
		String message = "Defender is hit for " + event.damage + " points (" + stats.getHealth() + "/" + stats.getBaseHealth() + ").";
		ui.showOverlayMessage(message, 1000);
	}
	
	/**
	 * Shows a message when an attack was dodged.
	 * 
	 * @param event	a {@code CombatEvent} describing the dodge
	 */
	@Subscribe
	private void onCombat(CombatEvent.Dodge event) {
		String message = "The attack missed.";
		ui.showOverlayMessage(message, 1000);
	}
	
	/**
	 * Shows a message when an attack was blocked.
	 * 
	 * @param event	a {@code CombatEvent} describing the block
	 */
	@Subscribe
	private void onCombat(CombatEvent.Block event) {
		String message = "The defender blocked the attack.";
		ui.showOverlayMessage(message, 1000);
	}
	
	/**
	 * Shows a message when a pickpocketing victim has no possessions.
	 * 
	 * @param event	a {@code StealthEvent} describing the failure
	 */
	@Subscribe
	private void onPickpocketFail(StealthEvent.Empty event) {
		ui.showOverlayMessage("Victim has no possessions.", 1000);
	}
	
	/**
	 * Shows a message when pickpocketing was successful.
	 * 
	 * @param event	a {@code StealthEvent} describing the success
	 */
	@Subscribe
	private void onPickpocketSuccess(StealthEvent.Stolen event) {
		ui.showOverlayMessage("You've stolen something.", 1000);
	}
	
	/**
	 * Shows a message when a lock was picked.
	 * 
	 * @param event	a {@code StealthEvent} describing the succes
	 */
	@Subscribe
	private void onLockpickSuccess(StealthEvent.Unlocked event) {
		ui.showOverlayMessage("You've picked a lock.", 1000);
	}
	
	/**
	 * Shows a message when a skill increased.
	 * 
	 * @param event	an {@code UpdateEvent} describing the increase
	 */
	@Subscribe
	private void onSkillIncrease(UpdateEvent.Skills event) {
		if (event.uid == Configuration.PLAYER_UID) {
			ui.showOverlayMessage(event.skill + " skill increased to " + event.value + ".", 1000);
		}
	}
	
	/**
	 * Shows a message on level up.
	 * 
	 * @param event	an {@code UpdateEvent} describing the level increase
	 */
	@Subscribe
	private void onLevelIncrease(UpdateEvent.Level event) {
		ui.showOverlayMessage("Level up!", 1000);
	}
	
	/**
	 * Shows a message on waking up.
	 * 
	 * @param event	a {@code RestEvent} describing the sleep
	 */
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
