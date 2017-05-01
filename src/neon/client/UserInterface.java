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

package neon.client;

import com.google.common.eventbus.Subscribe;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import neon.system.event.ClientConfigurationEvent;
import neon.system.event.MessageEvent;

/**
 * The user interface contains a number of JavaFX-related utility methods that 
 * can be used by modules. 
 * 
 * @author mdriesen
 *
 */
public class UserInterface {
	private final Stage stage;
	
	/**
	 * Initializes this {@code UserInterface} with the JavaFX {@code Stage} 
	 * that is to be used to show {@code Scene}s.
	 * 
	 * @param stage
	 */
	UserInterface(Stage stage) {
		this.stage = stage;
		
		stage.setWidth(1280);
		stage.setMinWidth(800);
		stage.setHeight(720);
		stage.setMinHeight(600);
		
		stage.show();
		stage.centerOnScreen();
	}
	
	/**
	 * Configures the user interface.
	 * 
	 * @param event an event containing configuration data
	 */
	@Subscribe
	private void configure(ClientConfigurationEvent event) {
		stage.setTitle(event.getTitle());
	}
	
	/**
	 * Shows a warning message in a dialog window.
	 * 
	 * @param event
	 */
	@Subscribe
	private void show(MessageEvent event) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText(event.getHeader());
		alert.setContentText(event.getMessage());
		alert.initOwner(stage);
		alert.showAndWait();
	}
	
	/**
	 * Sets the {@code Scene} to show on the JavaFX primary {@code Stage}.
	 * 
	 * @param scene	
	 */
	public void showScene(Scene scene) {
		stage.setScene(scene);
	}
}
