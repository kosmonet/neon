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

package neon.common.graphics;

import java.util.HashMap;
import java.util.logging.Logger;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceProvider;
import neon.entity.EntityProvider;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Player;
import neon.util.quadtree.RegionQuadTree;

/**
 * A {@code StackPane} for rendering the game world in multiple layers.
 * 
 * @author mdriesen
 *
 */
public class RenderPane extends StackPane {
	private final static Logger logger = Logger.getGlobal();
	
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private final ResourceProvider resources;
	private final EntityProvider entities;
	
	private RegionQuadTree<String> terrain;
	private RegionQuadTree<Integer> elevation;
	
	public RenderPane(ResourceProvider resources, EntityProvider entities) {
		this.resources = resources;
		this.entities = entities;
		double parallax = 1.02;
		
		for (int i = -5; i < 4; i++) {
			Canvas canvas = new RenderCanvas();
			layers.put(i, canvas);
			getChildren().add(canvas);			
			canvas.setScaleX(Math.pow(parallax, i));
			canvas.setScaleY(Math.pow(parallax, i));
			canvas.widthProperty().bind(widthProperty());
			canvas.heightProperty().bind(heightProperty());
		}
	}
	
	public void setMap(RegionQuadTree<String> terrain, RegionQuadTree<Integer> elevation) {
		this.terrain = terrain;
		this.elevation = elevation;
	}
	
	/**
	 * Redraws this pane.
	 * 
	 * @param xmin
	 * @param ymin
	 * @param scale
	 */
	public void draw(int xmin, int ymin, int scale) {
		for (Canvas canvas : layers.values()) {
			canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
		
		drawMap(xmin, ymin, scale);
		
		for (Entity entity : entities.getEntities()) {
			drawEntity(entity, xmin, ymin, scale);
		}
	}
	
	/**
	 * Draws the map.
	 * 
	 * @param xmin
	 * @param ymin
	 * @param scale
	 */
	private void drawMap(int xmin, int ymin, int scale) {
		for (int x = Math.max(0, xmin); x < Math.min(terrain.getWidth(), xmin + getWidth()/scale); x++) {
			for (int y = Math.max(0,  ymin); y < Math.min(terrain.getHeight(), ymin + getHeight()/scale); y++) {
				try {
					RTerrain rt = resources.getResource("terrain", terrain.get(x, y));
					GraphicsContext gc = layers.get(elevation.get(x, y)).getGraphicsContext2D();
					Image image = TextureFactory.getImage(scale, rt.getColor(), rt.getText());
					gc.drawImage(image, scale*(x - xmin), scale*(y - ymin));
				} catch (ResourceException e) {
					logger.warning(e.getMessage());
				}
			}
		}		
	}
	
	/**
	 * Draws the entities on the map.
	 * 
	 * @param entity
	 * @param xmin
	 * @param ymin
	 * @param scale
	 */
	private void drawEntity(Entity entity, int xmin, int ymin, int scale) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			GraphicsContext gc = layers.get(player.shape.getZ()).getGraphicsContext2D();
			Image image = TextureFactory.getImage(scale, player.graphics.getColor(), player.graphics.getText());
			gc.clearRect(scale*(player.shape.getX() - xmin) + 1, scale*(player.shape.getY() - ymin) + 1, scale - 1, scale - 1);
			gc.drawImage(image, scale*(player.shape.getX() - xmin), scale*(player.shape.getY() - ymin));
		} else if (entity instanceof Creature) {
			Creature creature = (Creature) entity;
			GraphicsContext gc = layers.get(creature.shape.getZ()).getGraphicsContext2D();
			Image image = TextureFactory.getImage(scale, creature.graphics.getColor(), creature.graphics.getText());
			gc.clearRect(scale*(creature.shape.getX() - xmin) + 1, scale*(creature.shape.getY() - ymin) + 1, scale - 1, scale - 1);
			gc.drawImage(image, scale*(creature.shape.getX() - xmin), scale*(creature.shape.getY() - ymin));			
		}
	}
}
