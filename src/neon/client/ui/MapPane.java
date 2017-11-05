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

import java.util.logging.Logger;

import javafx.scene.layout.Pane;

import neon.common.graphics.RenderCanvas;
import neon.common.resources.RMap;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceProvider;

/**
 * A pane to draw miniature maps.
 * 
 * @author mdriesen
 */
public class MapPane extends Pane {
	private static final Logger logger = Logger.getGlobal();
	
	private final ResourceProvider provider;
	private final RenderCanvas canvas = new RenderCanvas();
		
	public MapPane(ResourceProvider provider) {
		this.provider = provider;
	    canvas.widthProperty().bind(widthProperty());
	    canvas.heightProperty().bind(heightProperty());
	    getChildren().add(canvas);
	}
	
	public void drawMap(RMap map) {
		double width = map.getTerrain().getWidth();
		double height = map.getTerrain().getHeight();
		double scale = Math.max(width/getWidth(), height/getHeight());
		
		double xOffset = (int)(getWidth() - width/scale)/2;
		double yOffset = (int)(getHeight() - height/scale)/2;
		
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				String id = map.getTerrain().get((int) (x*scale), (int) (y*scale));
				if(id != null) {
					try {
						RTerrain terrain = provider.getResource("terrain", id);
						canvas.getGraphicsContext2D().setFill(terrain.getColor());
						canvas.getGraphicsContext2D().fillRect(x + xOffset, y + yOffset, 1, 1);
					} catch (ResourceException e) {
						logger.warning("unknown terrain type: " + id);
					}
				}
			}
		}
	}
}
