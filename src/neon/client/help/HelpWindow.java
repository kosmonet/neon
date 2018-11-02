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

package neon.client.help;

import java.io.IOException;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * A window to show help content. It uses the JavaFX {@code WebView} to show
 * help files in html format.
 * 
 * @author mdriesen
 *
 */
public final class HelpWindow {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private WebView view;
	
	private final Stage stage = new Stage();
	
	/**
	 * Initialize a new {@code HelpWindow} without content.
	 */
	public HelpWindow() {
		stage.setTitle("Help");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Help.fxml"));
		loader.setController(this);
		
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> stage.close());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load help window: " + e.getMessage());
		}
	}
	
	/**
	 * Shows the help window with html content.
	 * 
	 * @param content
	 */
	public void show(String content) {
        view.getEngine().load(getClass().getResource(content).toExternalForm());
        view.getEngine().setUserStyleSheetLocation(getClass().getResource("help.css").toString());
		stage.show();
	}
}
