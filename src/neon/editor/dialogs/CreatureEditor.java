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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import neon.common.resources.RCreature;
import neon.common.resources.ResourceException;
import neon.editor.Card;
import neon.editor.SaveEvent;
import neon.editor.help.HelpWindow;

/**
 * This resource editor shows a modal dialog window to edit the properties
 * of a creature:
 * <ul>
 * 	<li>the display name</li>
 * 	<li>the character</li>
 * 	<li>the color</li>
 * </ul>
 * 
 * The creature editor does not perform the actual saving to disk of the edited 
 * creature resource. Instead, it sends a {@code SaveEvent} to request 
 * that the creature resource be saved.
 * 
 * @author mdriesen
 *
 */
public class CreatureEditor {
	private static final Logger logger = Logger.getGlobal();

	@FXML private TextField nameField, textField;
	@FXML private ColorPicker colorBox;
	@FXML private Label previewLabel;
	@FXML private Spinner<Integer> speedSpinner;
	
	private final Stage stage = new Stage();
	private final EventBus bus;
	private final Card card;
	
	/**
	 * Initializes this {@code CreatureEditor}.
	 * 
	 * @param creature	the creature to edit
	 * @param mainStage	the parent stage for the dialog window
	 * @param bus		the {@code EventBus} used for messaging
	 * @throws ResourceException 
	 */
	public CreatureEditor(Card card, EventBus bus) throws ResourceException {
		this.card = card;
		RCreature creature = card.getResource();
		this.bus = bus;
		
		stage.setTitle("Creature properties");
		stage.initModality(Modality.APPLICATION_MODAL); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Creature.fxml"));
		loader.setController(this);
		
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../ui/editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load creature editor ui: " + e.getMessage());
		}
		
		previewLabel.setStyle("-fx-background-color: black;");
		previewLabel.setTextFill(creature.getColor());
		previewLabel.setText(creature.getCharacter());

		nameField.setText(creature.getName());
		textField.setText(creature.getCharacter());
		colorBox.setValue(creature.getColor());
		speedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
		speedSpinner.getValueFactory().setValue(creature.getSpeed());
		
		textField.textProperty().addListener((observable, oldValue, newValue) -> refresh());
		colorBox.valueProperty().addListener(value -> refresh());
	}
	
	/**
	 * Shows the creature editor window.
	 */
	public void show(Window parent) {
		stage.initOwner(parent);
		stage.show();
	}

	@FXML private void cancelPressed(ActionEvent event) {
		stage.close();
	}
	
	/**
	 * Refreshes the preview label if the text or color was changed.
	 */
	private void refresh() {
		previewLabel.setText(textField.getText());
		previewLabel.setTextFill(colorBox.getValue());
	}

	/**
	 * Saves changes to a new resource.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@FXML private void applyPressed(ActionEvent event) throws ResourceException {
		RCreature rc = card.getResource();
		String name = nameField.getText();
		Color color = colorBox.getValue();
		String text = textField.getText();
		speedSpinner.increment(0);	// stupid way to validate spinner value
		int speed = speedSpinner.getValue();
		// check if anything was actually changed
		if (!name.equals(rc.getName()) || !text.equals(rc.getCharacter()) || !color.equals(rc.getColor()) ||
				speed != rc.getSpeed()) {
			card.setRedefined(card.isOriginal() ? true : false);
			name = name.isEmpty() ? card.toString() : name;
			RCreature creature = new RCreature(card.toString(), name, text, color, speed);
			bus.post(new SaveEvent.Resources(creature));
			card.setChanged(true);				
		}
	}
	
//	private boolean isChanged() {
//		return !name.equals(rc.getName()) || !text.equals(rc.getCharacter()) || !color.equals(rc.getColor());
//	}
	
	@FXML private void okPressed(ActionEvent event) throws ResourceException {
		applyPressed(event);
		cancelPressed(event);
	}
	
	@FXML private void helpPressed(ActionEvent event) {
		new HelpWindow().show("creatures.html");
	}
}
