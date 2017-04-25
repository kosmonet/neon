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

package neon.client.graphics;

import java.util.HashMap;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import neon.util.quadtree.RegionQuadTree;

/**
 * A {@code StackPane} for rendering the game world in multiple layers.
 * 
 * @author mdriesen
 *
 */
public class RenderPane extends StackPane {
	private final TextureFactory factory = new TextureFactory();
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private RegionQuadTree<Color> terrain;
	private RegionQuadTree<Integer> depth;

	public RenderPane() {
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
		
		// TODO: brightness en/of saturation filters op elk canvas
	}
	
	public void setMap(RegionQuadTree<Color> terrain, RegionQuadTree<Integer> depth) {
		this.terrain = terrain;
		this.depth = depth;
	}
	
    public void draw(int xpos, int ypos) {
    	for (Canvas canvas : layers.values()) {
    		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	}

    	for (int x = xpos; x < 100; x++) {
    		for (int y = ypos; y < 100; y++) {
    			GraphicsContext gc = layers.get(depth.get(x, y)).getGraphicsContext2D();
    			gc.setFill(Color.GREY);
   				gc.setFill(terrain.get(x, y));
    			gc.drawImage(factory.getImage(30, gc.getFill(), "g"), 30*(x - xpos), 30*(y - ypos), 30, 30);
    		}
    	}
    }
}
