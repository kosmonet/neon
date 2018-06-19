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

import java.util.Comparator;
import java.util.HashMap;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import neon.common.graphics.EntityRenderer;
import neon.common.graphics.TextureFactory;
import neon.entity.components.Graphics;
import neon.entity.components.Shape;
import neon.entity.entities.Entity;
import neon.entity.entities.Item;

public class ClientRenderer implements EntityRenderer<Entity> {
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private final EntityComparator comparator = new EntityComparator();
	
	@Override
	public void drawEntity(Entity entity, int xmin, int ymin, int scale) {
		Shape shape = entity.getComponent(Shape.class);
		Graphics graphics = entity.getComponent(Graphics.class);
		
		GraphicsContext gc = layers.get(shape.getZ()).getGraphicsContext2D();
		Image image = TextureFactory.getImage(scale, graphics.getColor(), graphics.getGlyph());
		gc.clearRect(scale*(shape.getX() - xmin) + 1, scale*(shape.getY() - ymin) + 1, scale - 1, scale - 1);
		gc.drawImage(image, scale*(shape.getX() - xmin), scale*(shape.getY() - ymin));
	}

	@Override
	public void setLayers(HashMap<Integer, Canvas> layers) {
		this.layers.clear();
		this.layers.putAll(layers);
	}

	@Override
	public Comparator<Entity> getComparator() {
		return comparator;
	}
	
	private class EntityComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity one, Entity two) {
			if (one.equals(two)) {
				return 0;
			} else {
				return (two instanceof Item) ? 1 : -1;
			}
		}		
	}
}
