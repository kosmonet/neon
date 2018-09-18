/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

package neon.client.states;

import java.io.IOException;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class CutSceneState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	private Scene scene;

	public CutSceneState() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Scene.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
//			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		} catch (IOException e) {
			logger.severe("failed to load inventory interface: " + e.getMessage());
		}
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering container module");
	}

	@Override
	public void exit(TransitionEvent event) {
	}
}
