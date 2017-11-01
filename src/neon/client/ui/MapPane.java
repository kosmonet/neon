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

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;

import neon.common.graphics.RenderCanvas;
import neon.common.resources.RMap;
import neon.common.resources.RTerrain;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceProvider;

/**
 * Pane to draw miniature maps.
 * 
 * @author mdriesen
 */
public class MapPane extends AnchorPane {
	private static final Logger logger = Logger.getGlobal();
	
	private final Canvas canvas = new RenderCanvas();
	private final ResourceProvider provider;
		
	public MapPane(ResourceProvider provider) {
		this.provider = provider;
		getChildren().add(canvas);
		setBottomAnchor(canvas, 0d);
		setTopAnchor(canvas, 0d);
		setLeftAnchor(canvas, 0d);
		setRightAnchor(canvas, 0d);
		
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());
	}
	
	public void drawMap(RMap map) {
		int width = map.getTerrain().getWidth();
		int height = map.getTerrain().getWidth();
		double scale = Math.max(width/canvas.getWidth(), height/canvas.getHeight());
		
		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {
				String id = map.getTerrain().get((int) (x*scale), (int) (y*scale));
				if(id != null) {
					try {
						RTerrain terrain = provider.getResource("terrain", id);
						canvas.getGraphicsContext2D().setFill(terrain.getColor());
						canvas.getGraphicsContext2D().fillRect(x, y, 1, 1);
					} catch (ResourceException e) {
						logger.warning("unknown terrain type: " + id);
					}
				}
			}
		}
	}
}
