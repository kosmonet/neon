/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

import neon.common.graphics.TextureFactory;
import neon.entity.entities.Item;

/**
 * A custom label used to show item descriptions in the inventory module.
 * 
 * @author mdriesen
 *
 */
public class DescriptionLabel extends Label {
	public DescriptionLabel() {
		setTextAlignment(TextAlignment.CENTER);
		setContentDisplay(ContentDisplay.TOP);
		setMaxWidth(Double.MAX_VALUE);
		setMaxHeight(Double.MAX_VALUE);
	}

	/**
	 * Updates the description.
	 * 
	 * @param item	the item to describe
	 */
	public void update(Item item) {
		if(item != null) {
			// create the image like it would show in-game on the ground
			Image image = TextureFactory.getImage(60, item.graphics.getColor(), item.graphics.getText());
			setGraphic(new ImageView(image));
			
			StringBuffer description = new StringBuffer();
			description.append("\n");
			description.append(item);
			description.append("\n");

			setText(description.toString());
		} else {
			setGraphic(null);
			setText(null);
		}
	}
}