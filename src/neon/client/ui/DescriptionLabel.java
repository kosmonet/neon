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

package neon.client.ui;

import com.google.common.collect.ClassToInstanceMap;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.Component;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.ItemInfo;
import neon.common.graphics.TextureFactory;
import neon.systems.combat.Armor;
import neon.systems.combat.Weapon;
import neon.systems.magic.Enchantment;

/**
 * A custom label to show item and creature descriptions.
 * 
 * @author mdriesen
 *
 */
public final class DescriptionLabel extends Label {
	/**
	 * Initialize this label.
	 */
	public DescriptionLabel() {
		setTextAlignment(TextAlignment.CENTER);
		setContentDisplay(ContentDisplay.TOP);
		setMaxWidth(Double.MAX_VALUE);
		setMaxHeight(Double.MAX_VALUE);
	}
	
	/**
	 * Updates the description of a creature.
	 * 
	 * @param components	all the components that make a creature
	 */
	public void updateCreature(ClassToInstanceMap<Component> components) {
		if (components.containsKey(Graphics.class)) {
			// create the image like it would show in-game on the ground
			Graphics graphics = components.getInstance(Graphics.class);
			Image image = TextureFactory.getImage(60, graphics.getColor(), graphics.getGlyph());
			setGraphic(new ImageView(image));
		} else {
			setGraphic(null);
		}
		
		StringBuilder description = new StringBuilder();
		if (components.containsKey(CreatureInfo.class)) {
			CreatureInfo info = components.getInstance(CreatureInfo.class);
			description.append("\n");
			description.append(info.getName());
			description.append("\n");
		}
		
		setText(description.toString());			
	}

	/**
	 * Updates the description of an item.
	 * 
	 * @param components	all the components that make an item
	 */
	public void updateItem(ClassToInstanceMap<Component> components) {
		if (components.containsKey(Graphics.class)) {
			// create the image like it would show in-game on the ground
			Graphics graphics = components.getInstance(Graphics.class);
			Image image = TextureFactory.getImage(60, graphics.getColor(), graphics.getGlyph());
			setGraphic(new ImageView(image));	
		} else {
			setGraphic(null);
		}

		StringBuilder builder = new StringBuilder();
		if (components.containsKey(ItemInfo.class)) {
			ItemInfo info = components.getInstance(ItemInfo.class);
			builder.append(info.name);
			builder.append("\n");
			builder.append("weight: " + (float) info.weight/100 + " stone");
			builder.append("\n");	
			builder.append("price: " + info.price + " cp");
			builder.append("\n");
		}

		if (components.containsKey(Clothing.class)) {
			Clothing clothing = components.getInstance(Clothing.class);
			builder.append("∷\n");
			builder.append("Slot: " + clothing.getSlot().toString().toLowerCase());
		}

		if (components.containsKey(Armor.class)) {
			Armor armor = components.getInstance(Armor.class);
			builder.append("\n");
			builder.append("Rating: " + armor.getRating());						
		}

		if (components.containsKey(Weapon.class)) {
			Weapon weapon = components.getInstance(Weapon.class);
			builder.append("∷\n");
			builder.append("Damage: " + weapon.getDamage());						
		}

		if (components.containsKey(Enchantment.class)) {
			Enchantment enchantment = components.getInstance(Enchantment.class);
			builder.append("\n∷\n");
			builder.append("Effect: " + enchantment.getEffect() + "\n");						
			builder.append("Magnitude: " + enchantment.getMagnitude());						
		}

		setText(builder.toString());
	}
}