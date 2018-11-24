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

package neon.common.graphics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.util.spatial.RegionSpatialIndex;

/**
 * A {@code StackPane} for rendering the game world in multiple layers.
 * 
 * @author mdriesen
 *
 */
public final class RenderPane<T> extends StackPane {
	private static final Logger logger = Logger.getGlobal();
//	private static final double parallax = 1.02;
	
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private final ResourceManager resources;
	private final EntityRenderer<T> renderer;
	
	private RegionSpatialIndex<String> terrain;
	private RegionSpatialIndex<Integer> elevation;
	private SortedSet<T> entities;
	
	public RenderPane(ResourceManager resources, EntityRenderer<T> renderer) {
		this.resources = resources;
		this.renderer = renderer;
		entities = new TreeSet<>(renderer.getComparator());
		
		for (int i = -5; i < 4; i++) {
			Canvas canvas = new RenderCanvas();
			layers.put(i, canvas);
			getChildren().add(canvas);			
//			canvas.setScaleX(Math.pow(parallax, i));
//			canvas.setScaleY(Math.pow(parallax, i));
			canvas.widthProperty().bind(widthProperty());
			canvas.heightProperty().bind(heightProperty());
		}
		
		renderer.setLayers(layers);
	}
	
	public void setMap(RegionSpatialIndex<String> terrain, RegionSpatialIndex<Integer> elevation, Collection<? extends T> entities) {
		this.terrain = terrain;
		this.elevation = elevation;
		this.entities.clear();
		this.entities.addAll(entities);
		
	}
	
	public void updateMap(Collection<? extends T> entities) {
		this.entities.clear();
		this.entities.addAll(entities);
	}
	
	/**
	 * Redraws this pane.
	 * 
	 * @param xmin
	 * @param ymin
	 * @param scale
	 */
	public void draw(int xmin, int ymin, int scale) {
		for (Map.Entry<Integer, Canvas> entry : layers.entrySet()) {
			Canvas canvas = entry.getValue();
			canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			ColorAdjust darken = new ColorAdjust();
			darken.setBrightness(-0.2*entry.getKey());
			canvas.setEffect(darken);
		}
		
		drawMap(xmin, ymin, scale);
		entities.stream().forEach(entity -> renderer.drawEntity(entity, xmin, ymin, scale));
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
					Image image = TextureFactory.getImage(scale, rt.color, rt.glyph);
					gc.drawImage(image, scale*(x - xmin), scale*(y - ymin));
				} catch (ResourceException e) {
					logger.warning(e.getMessage());
				}
			}
		}		
	}
	
	/**
	 * A resizable JavaFX {@code Canvas}.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class RenderCanvas extends Canvas {
		@Override
		public boolean isResizable() {
		    return true;
		}
	}
}
