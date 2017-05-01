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

package neon.editor.dialogs;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import neon.editor.Card;
import neon.editor.SaveEvent;
import neon.system.resources.RTerrain;
import neon.system.resources.ResourceException;

/**
 * An editor for terrain resources.
 * 
 * @author mdriesen
 *
 */
public class TerrainEditor {
	private static final Logger logger = Logger.getGlobal();

	@FXML private TextField nameField, textField;
	@FXML private ColorPicker colorBox;
	@FXML private Label previewLabel;

	private final Stage stage = new Stage();
	private final EventBus bus;
	private final Card card;
	
	public TerrainEditor(Card card, EventBus bus) throws ResourceException {
		this.card = card;
		RTerrain terrain = card.getResource();
		this.bus = bus;
		
		stage.setTitle("Terrain properties");
		stage.initModality(Modality.APPLICATION_MODAL); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Terrain.fxml"));
		loader.setController(this);
		
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../ui/editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load terrain editor ui" + e.getMessage());
		}
		
		nameField.setText(terrain.getName());
		textField.setText(terrain.getText());
		colorBox.setValue(terrain.getColor());
		
		previewLabel.setStyle("-fx-background-color: black;");
		previewLabel.setTextFill(terrain.getColor());
		previewLabel.setText(terrain.getText());
		
		textField.textProperty().addListener((observable, oldValue, newValue) -> refresh());
		colorBox.valueProperty().addListener(value -> refresh());
	}
	
	/**
	 * Refreshes the preview label if the terrain text or color was changed.
	 */
	private void refresh() {
		previewLabel.setText(textField.getText());
		previewLabel.setTextFill(colorBox.getValue());
	}
	
	/**
	 * Shows the terrain editor window.
	 */
	public void show(Window parent) {
		stage.initOwner(parent);
		stage.show();
	}

	@FXML private void cancelPressed(ActionEvent event) {
		stage.close();
	}
	
	/**
	 * Saves changes to a new resource.
	 * 
	 * @param event
	 */
	@FXML private void applyPressed(ActionEvent event) {
		String name = nameField.getText().isEmpty() ? card.toString() : nameField.getText();
		String text = textField.getText();
		Color color = colorBox.getValue();
		
		RTerrain terrain = new RTerrain(card.toString(), name, text, color);
		bus.post(new SaveEvent.Resources("terrain", terrain));
		card.setChanged(true);
	}
	
	@FXML private void okPressed(ActionEvent event) {
		applyPressed(event);
		cancelPressed(event);
	}
}
