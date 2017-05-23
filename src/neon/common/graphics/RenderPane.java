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
import neon.common.resources.RMap;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceProvider;
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
	
	private RegionQuadTree<String> terrain;
	private RegionQuadTree<Integer> elevation;
	
	public RenderPane(ResourceProvider resources) {
		this.resources = resources;
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
	
	public void setMap(RMap map) {
		terrain = map.getTerrain();
		elevation = map.getElevation();
	}
	
	public void draw(int xmin, int ymin, int scale) {
		for (Canvas canvas : layers.values()) {
			canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}

		for (int x = xmin; x < Math.min(terrain.getWidth(), xmin + getWidth()/scale); x++) {
			for (int y = ymin; y < Math.min(terrain.getHeight(), ymin + getHeight()/scale); y++) {
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
}
