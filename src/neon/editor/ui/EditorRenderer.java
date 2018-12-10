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

package neon.editor.ui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import neon.common.entity.Entity;
import neon.common.entity.components.ItemInfo;
import neon.common.graphics.EntityRenderer;
import neon.common.graphics.TextureFactory;
import neon.editor.resource.ICreature;

public class EditorRenderer implements EntityRenderer<Entity> {
	private final Map<Integer, Canvas> layers = new HashMap<>();
	private final EntityComparator comparator = new EntityComparator();

	@Override
	public void drawEntity(Entity entity, int xmin, int ymin, int scale) {
		if (entity instanceof ICreature) {
			ICreature creature = (ICreature) entity;
			GraphicsContext gc = layers.get(creature.shape.getZ()).getGraphicsContext2D();
			Image image = TextureFactory.getImage(scale, creature.graphics.getColor(), creature.graphics.getGlyph());
			gc.clearRect(scale*(creature.shape.getX() - xmin) + 1, scale*(creature.shape.getY() - ymin) + 1, scale - 1, scale - 1);
			gc.drawImage(image, scale*(creature.shape.getX() - xmin), scale*(creature.shape.getY() - ymin));			
		}		
	}

	@Override
	public void setLayers(Map<Integer, Canvas> layers) {
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
				return (two.hasComponent(ItemInfo.class)) ? 1 : -1;
			}
		}		
	}
}
