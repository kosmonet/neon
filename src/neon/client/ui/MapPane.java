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

import java.util.Objects;
import java.util.logging.Logger;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import neon.client.Map;
import neon.client.Map.Marker;
import neon.common.graphics.RenderCanvas;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

/**
 * A pane to draw miniature maps.
 * 
 * @author mdriesen
 */
public final class MapPane extends Pane {
	private static final Logger logger = Logger.getGlobal();
	
	private final ResourceManager resources;
	private final Canvas canvas = new RenderCanvas();
	
	public MapPane(ResourceManager resources) {
		this.resources = Objects.requireNonNull(resources, "resource manager");
	    canvas.widthProperty().bind(widthProperty());
	    canvas.heightProperty().bind(heightProperty());
		canvas.getGraphicsContext2D().setFont(Font.font(18));
	    getChildren().add(canvas);
	}
	
	/**
	 * Draws the given map.
	 * 
	 * @param map
	 */
	public void drawMap(Map map) {
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		double width = map.getTerrain().getWidth();
		double height = map.getTerrain().getHeight();
		double scale = Math.max(width/getWidth(), height/getHeight());
		
		// center on the screen
		double xOffset = (int)(getWidth() - width/scale)/2;
		double yOffset = (int)(getHeight() - height/scale)/2;
		
		// draw the terrain
		for (int x = 0; x < getWidth() - xOffset*2; x++) {
			for (int y = 0; y < getHeight() - yOffset*2; y++) {
				String id = map.getTerrain().get((int) (x*scale), (int) (y*scale));
				if (id != null) {
					try {
						RTerrain terrain = resources.getResource("terrain", id);
						canvas.getGraphicsContext2D().setFill(terrain.color);
						canvas.getGraphicsContext2D().fillRect(x + xOffset, y + yOffset, 1, 1);
					} catch (ResourceException e) {
						logger.warning("unknown terrain type: " + id);
					}
				}
			}
		}
		
		// draw the markers
		for (Marker marker : map.getMarkers()) {
			canvas.getGraphicsContext2D().setFill(Color.DARKSLATEGRAY);
			canvas.getGraphicsContext2D().fillText("↙" + marker.text, marker.x/scale + xOffset, marker.y/scale + yOffset);
		}
	}
}
