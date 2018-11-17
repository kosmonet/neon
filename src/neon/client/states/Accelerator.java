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

package neon.client.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import neon.client.Configuration;
import neon.client.Map;
import neon.client.ui.ButtonTypes;
import neon.client.ui.UserInterface;
import neon.client.ComponentManager;
import neon.common.entity.PlayerMode;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Lock;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.event.InputEvent;
import neon.common.event.StealthEvent;
import neon.systems.magic.Enchantment;
import neon.systems.magic.MagicEvent;

/**
 * This class provides some accelerators for the main game scene.
 * 
 * @author mdriesen
 *
 */
class Accelerator {
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final UserInterface ui;
	private final ComponentManager components;
	private final Configuration config;
	
	public Accelerator(UserInterface ui, EventBus bus, ComponentManager components, Configuration config) {
		this.bus = bus;
		this.ui = ui;
		this.components = components;
		this.config = config;
	}
	
	public void act() {
		// check if there's another entity besides the player on the given position
		Map map = config.getCurrentMap();
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		List<Long> entities = map.getEntities(shape.getX(), shape.getY()).stream()
				.filter(uid -> uid != PLAYER_UID).collect(Collectors.toList());
		
		if (entities.size() > 1) {
			bus.post(new TransitionEvent("pick"));			
		} else {
			if (components.hasComponent(entities.get(0), Lock.class)) {
				Lock lock = components.getComponent(entities.get(0), Lock.class);
				if (lock.isLocked()) {
					pickLock(lock);
				} else {
					bus.post(new TransitionEvent("pick"));					
				}
			} else {
				bus.post(new TransitionEvent("pick"));				
			}
		}
	}
	
	private void pickLock(Lock lock) {
		// pause the server
		if (!config.isPaused()) {
			bus.post(new InputEvent.Pause());
		}
		
		Optional<ButtonType> result = ui.showQuestion("Try to pick lock?", 
				ButtonTypes.yes, ButtonTypes.no);

		if (result.get().equals(ButtonTypes.yes)) {
			// server takes care of saving
			bus.post(new StealthEvent.Unlock(lock.getEntity()));
		} 
		
		// unpause if necessary
		if (!config.isPaused()) {
			bus.post(new InputEvent.Unpause());
		}		
	}
	
	/**
	 * Changes the player mode.
	 * 
	 * @param modeLabel
	 */
	public void changeMode(Label modeLabel) {
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
	
	/**
	 * Lets the player choose an enchanted item to use.
	 */
	public void use() {
		Equipment equipment = components.getComponent(PLAYER_UID, Equipment.class);
		ArrayList<ButtonType> items = new ArrayList<>();
		HashMap<ButtonType, Long> mapping = new HashMap<>();
		
		for (long item : equipment.getEquippedItems()) {
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
	
	/**
	 * Asks the player to quit the game.
	 */
	public void quit() {
		// pause the server
		if (!config.isPaused()) {
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
		if (!config.isPaused()) {
			bus.post(new InputEvent.Unpause());
		}
	}
}
