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

package neon.client.modules;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import neon.client.UserInterface;
import neon.system.graphics.RenderPane;
import neon.util.quadtree.RegionQuadTree;

public class GameModule extends Module {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private RenderPane pane;
	
	private final UserInterface ui;
	private Scene scene;
	private RegionQuadTree<Color> tree = new RegionQuadTree<>(100, 100);
	private RegionQuadTree<Integer> depth = new RegionQuadTree<>(100, 100);	
	private int xpos = 0, ypos = 0;
	
	public GameModule(UserInterface ui) {
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/Game.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../scenes/main.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("failed to load new game menu");
		}
		
		// also quit when pressing esc
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT), () -> move("left"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT), () -> move("right"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP), () -> move("up"));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN), () -> move("down"));
		
//		pane.setMap(tree, depth);
		scene.widthProperty().addListener((observable, oldWidth, newWidth) -> pane.draw(xpos, ypos));
		scene.heightProperty().addListener((observable, oldHeight, newHeight) -> pane.draw(xpos, ypos));
	}
	
	private void move(String dir) {
		switch (dir) {
		case "left": xpos = Math.max(0, xpos - 1); break;
		case "right": xpos++; break;
		case "up": ypos = Math.max(0, ypos - 1); break;
		case "down": ypos++; break;
		}
		pane.draw(xpos, ypos);
	}
	
	@Override
	public void enter(TransitionEvent event) {
		// start drawing only after scene size is set
    	tree.add(new Rectangle(5, 5), Color.RED);
    	tree.add(new Rectangle(10, 1), Color.BLUE);
    	tree.add(new Rectangle(20, 10, 5, 5), Color.GREEN);
    	tree.add(new Rectangle(10, 20, 10, 5), Color.WHITE);
    	depth.add(new Rectangle(0, 0, 100, 100), 0);
    	depth.add(new Rectangle(20, 10, 5, 5), 3);
    	depth.add(new Rectangle(10, 20, 5, 5), -5);
    	
		ui.showScene(scene);
		
		pane.draw(xpos, ypos);
		logger.finest("entering game module");
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting game module");
	}
}
