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

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import neon.common.event.ClientConfigurationEvent;
import neon.common.event.MessageEvent;

/**
 * The user interface contains a number of JavaFX-related utility methods that 
 * can be used by modules. 
 * 
 * @author mdriesen
 *
 */
public class UserInterface {
	private static final Logger logger = Logger.getGlobal();
	
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
		
		// show default loading screen
		FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/Splash.fxml"));
		loader.setController(this);
		
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("scenes/main.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load loading screen :-/ " + e.getMessage());
		}
		
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
	 * Shows a warning message in a proper dialog window.
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
	 * Shows a message in a semi-transparent dialog for the given duration.
	 * 
	 * @param message	the message that will be shown
	 * @param duration	how long the message will be shown (in milliseconds)
	 */
	public void showMessage(String message, int duration) {
		Dialog<Boolean> dialog = new Dialog<>();
		dialog.initOwner(stage);
		dialog.getDialogPane().setContentText(message);
		dialog.getDialogPane().getScene().setFill(null);
		dialog.initStyle(StageStyle.TRANSPARENT);
		dialog.show();
		
		// dialog closes when a result is set, dialog.close() won't work without an actual close button
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(duration), event -> dialog.setResult(true)));
		timeline.setCycleCount(1);
		timeline.play();
	}
	
	/**
	 * Shows a question and possible answers.
	 * 
	 * @param message
	 * @return
	 */
	public Optional<ButtonType> showQuestion(String message, ButtonType... buttons) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.getButtonTypes().setAll(buttons);
		alert.getDialogPane().getScene().setFill(null);
		alert.initStyle(StageStyle.TRANSPARENT);
		alert.setGraphic(null);

		return alert.showAndWait();
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
