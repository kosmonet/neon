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
import java.util.logging.Logger;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import neon.client.ComponentManager;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Shape;
import neon.common.graphics.EntityRenderer;
import neon.common.graphics.TextureFactory;

public class ClientRenderer implements EntityRenderer<Long> {
	private static final Logger logger = Logger.getGlobal();
	
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private final EntityComparator comparator = new EntityComparator();
	private final ComponentManager components;
	
	public ClientRenderer(ComponentManager components) {
		this.components = components;
	}
	
	@Override
	public void drawEntity(Long uid, int xmin, int ymin, int scale) {
		try {
			Shape shape = components.getComponent(uid, Shape.class);
			Graphics graphics = components.getComponent(uid, Graphics.class);
			GraphicsContext gc = layers.get(shape.getZ()).getGraphicsContext2D();
			Image image = TextureFactory.getImage(scale, graphics.getColor(), graphics.getGlyph());
			gc.clearRect(scale*(shape.getX() - xmin) + 1, scale*(shape.getY() - ymin) + 1, scale - 1, scale - 1);
			gc.drawImage(image, scale*(shape.getX() - xmin), scale*(shape.getY() - ymin));
		} catch (NullPointerException e) {
			logger.severe("could not render entity " + uid);
		}
	}

	@Override
	public void setLayers(HashMap<Integer, Canvas> layers) {
		this.layers.clear();
		this.layers.putAll(layers);
	}

	@Override
	public Comparator<Long> getComparator() {
		return comparator;
	}
	
	private class EntityComparator implements Comparator<Long> {
		@Override
		public int compare(Long one, Long two) {
			if (one.equals(two)) {
				return 0;
			} else {
				return components.hasComponent(two, ItemInfo.class) ? 1 : -1;
			}
		}		
	}
}
