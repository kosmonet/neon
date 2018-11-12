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

import com.google.common.eventbus.Subscribe;

import neon.client.ComponentManager;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Stats;
import neon.common.event.StealthEvent;
import neon.common.event.UpdateEvent;
import neon.common.resources.ResourceException;
import neon.systems.combat.CombatEvent;

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
		this.ui = ui;
		this.components = components;
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
	private void onPickpocketSuccess(StealthEvent.Success event) {
		ui.showOverlayMessage("You've stolen something.", 1000);
	}
	
	@Subscribe
	private void onSkillIncrease(UpdateEvent.Skill event) throws ResourceException {
		if (event.uid == PLAYER_UID) {
			ui.showOverlayMessage(event.skill + " skill increased to " + event.value + ".", 1000);
		}
	}
	
	@Subscribe
	private void onLevelIncrease(UpdateEvent.Level event) throws ResourceException {
		ui.showOverlayMessage("Level up!", 1000);
	}
}
