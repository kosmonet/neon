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

import java.util.Objects;

import javafx.scene.control.ListCell;
import neon.client.ComponentManager;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.ItemInfo;
import neon.systems.combat.Weapon;
import neon.systems.magic.Enchantment;

/**
 * A custom {@code ListCell<Long>} to render the items in the player's inventory.
 * 
 * @author mdriesen
 *
 */
public final class ItemCell extends ListCell<Long> {
	private static final long PLAYER_UID = 0;
	
	private final ComponentManager components;
	
	public ItemCell(ComponentManager components) {
		this.components = Objects.requireNonNull(components, "component manager");
	}
	
	@Override
	public void updateItem(Long uid, boolean empty) {
		super.updateItem(uid, empty);
		
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			StringBuilder style = new StringBuilder();
			
			if (components.hasComponent(uid, Enchantment.class)) {
				style.append(isSelected() ? "-fx-text-fill: turquoise;" : "-fx-text-fill: teal;");    				
			} else {
				style.append(isSelected() ? "-fx-text-fill: white;" : "-fx-text-fill: silver;");
			}
			
			Equipment equipment = components.getComponent(PLAYER_UID, Equipment.class);
			if (equipment.hasEquipped(uid)) {
				style.append("-fx-font-weight: bold;");    				
			} else {
				style.append("-fx-font-weight: normal;");    				
			}
			
			setStyle(style.toString());
			
			StringBuilder text = new StringBuilder();
			
			if (components.hasComponent(uid, Weapon.class)) {
				text.append("⚔ ");
			}
			
			ItemInfo info = components.getComponent(uid, ItemInfo.class);
			text.append(info.name);
			setText(text.toString());
		}
	}
}
