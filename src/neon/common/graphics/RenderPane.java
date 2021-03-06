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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
	
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private final ResourceManager resources;
	private final EntityRenderer<T> renderer;
	
	private RenderableMap<? extends T> map;
	
	/**
	 * The resource manager and entity renderer must not be null.
	 * 
	 * @param resources	a {@code ResourceManager}
	 * @param renderer	the {@code EntityRenderer} to use
	 */
	public RenderPane(ResourceManager resources, EntityRenderer<T> renderer) {
		this.resources = Objects.requireNonNull(resources, "resource manager");
		this.renderer = Objects.requireNonNull(renderer, "renderer");
		
		for (int i = -5; i < 4; i++) {
			Canvas canvas = new RenderCanvas();
			layers.put(i, canvas);
			getChildren().add(canvas);			
			canvas.widthProperty().bind(widthProperty());
			canvas.heightProperty().bind(heightProperty());
		}
		
		renderer.setLayers(layers);
	}
	
	/**
	 * Sets the map to be rendered. The map must not be null.
	 * 
	 * @param map	a {@code RenderableMap}
	 */
	public void setMap(RenderableMap<? extends T> map) {
		this.map = Objects.requireNonNull(map, "map");
		logger.fine("setting new map on render pane: " + map.getId());
	}
	
	/**
	 * Redraws this pane. A scale of 1 means that every entity will be 
	 * rendered as 1x1 pixels. A scale of 10 will render as 10x10 pixels. 
	 * 
	 * @param xmin	the leftmost visible map position on the screen
	 * @param ymin	the topmost visible map position on the screen
	 * @param scale	a scale factor
	 */
	public void draw(int xmin, int ymin, int scale) {
		for (Map.Entry<Integer, Canvas> entry : layers.entrySet()) {
			Canvas canvas = entry.getValue();
			canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			ColorAdjust darken = new ColorAdjust();
			darken.setBrightness(Math.min(0, 0.2*entry.getKey()));
			canvas.setEffect(darken);
		}
		
		drawMap(xmin, ymin, scale);
		
		map.getEntities().parallelStream().sorted(renderer.getComparator()).sequential()
				.forEach(entity -> renderer.drawEntity(entity, xmin, ymin, scale));
	}
	
	/**
	 * Draws the map. A scale of 1 means that every terrain tile will be 
	 * rendered as 1x1 pixels. A scale of 10 will render as 10x10 pixels.
	 * 
	 * @param xmin	the leftmost visible map position on the screen
	 * @param ymin	the topmost visible map position on the screen
	 * @param scale	a scale factor
	 */
	private void drawMap(int xmin, int ymin, int scale) {
		RegionSpatialIndex<String> terrain = map.getTerrain();
		RegionSpatialIndex<Integer> elevation = map.getElevation();
		
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
}
