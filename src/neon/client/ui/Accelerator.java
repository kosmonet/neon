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

package neon.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import neon.client.Configuration;
import neon.client.ComponentManager;
import neon.client.states.TransitionEvent;
import neon.common.entity.PlayerMode;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.event.InputEvent;
import neon.common.resources.RMap;
import neon.systems.magic.Enchantment;
import neon.systems.magic.MagicEvent;

/**
 * This class provides some accelerators for the main game scene.
 * 
 * @author mdriesen
 *
 */
public class Accelerator {
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
		RMap map = config.getCurrentMap();
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		if (map.getEntities(shape.getX(), shape.getY()).size() > 1) {
			bus.post(new TransitionEvent("pick", map));			
		}
	}
	
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
	
	public void use() {
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
