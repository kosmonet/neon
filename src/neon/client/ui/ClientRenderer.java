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

import java.util.HashMap;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import neon.common.graphics.EntityRenderer;
import neon.common.graphics.TextureFactory;
import neon.entity.entities.Creature;
import neon.entity.entities.Player;

public class ClientRenderer implements EntityRenderer {
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	
	@Override
	public void drawEntity(Object entity, int xmin, int ymin, int scale) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			GraphicsContext gc = layers.get(player.shape.getZ()).getGraphicsContext2D();
			Image image = TextureFactory.getImage(scale, player.graphics.getColor(), player.graphics.getGlyph());
			gc.clearRect(scale*(player.shape.getX() - xmin) + 1, scale*(player.shape.getY() - ymin) + 1, scale - 1, scale - 1);
			gc.drawImage(image, scale*(player.shape.getX() - xmin), scale*(player.shape.getY() - ymin));
		} else if (entity instanceof Creature) {
			Creature creature = (Creature) entity;
			GraphicsContext gc = layers.get(creature.shape.getZ()).getGraphicsContext2D();
			Image image = TextureFactory.getImage(scale, creature.graphics.getColor(), creature.graphics.getGlyph());
			gc.clearRect(scale*(creature.shape.getX() - xmin) + 1, scale*(creature.shape.getY() - ymin) + 1, scale - 1, scale - 1);
			gc.drawImage(image, scale*(creature.shape.getX() - xmin), scale*(creature.shape.getY() - ymin));			
		}
	}

	@Override
	public void setLayers(HashMap<Integer, Canvas> layers) {
		this.layers.clear();
		this.layers.putAll(layers);
	}
}
