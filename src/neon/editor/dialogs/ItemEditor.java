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
import neon.common.resources.RItem;
import neon.common.resources.ResourceException;
import neon.editor.Card;
import neon.editor.SaveEvent;
import neon.editor.help.HelpWindow;

/**
 * This resource editor shows a modal dialog window to edit the properties
 * of a item:
 * <ul>
 * 	<li>the display name</li>
 * 	<li>the character</li>
 * 	<li>the color</li>
 * </ul>
 * 
 * The item editor does not perform the actual saving to disk of the edited 
 * item resource. Instead, it sends a {@code SaveEvent} to request 
 * that the item resource be saved.
 * 
 * @author mdriesen
 *
 */
public class ItemEditor {
	private static final Logger logger = Logger.getGlobal();

	@FXML private TextField nameField, textField;
	@FXML private ColorPicker colorBox;
	@FXML private Label previewLabel;
	
	private final Stage stage = new Stage();
	private final EventBus bus;
	private final Card card;
	
	/**
	 * Initializes this {@code ItemEditor}.
	 * 
	 * @param item	the item to edit
	 * @param mainStage	the parent stage for the dialog window
	 * @param bus		the {@code EventBus} used for messaging
	 * @throws ResourceException 
	 */
	public ItemEditor(Card card, EventBus bus) throws ResourceException {
		this.card = card;
		RItem item = card.getResource();
		this.bus = bus;
		
		stage.setTitle("Item properties");
		stage.initModality(Modality.APPLICATION_MODAL); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Item.fxml"));
		loader.setController(this);
		
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../ui/editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load item editor ui: " + e.getMessage());
		}
		
		previewLabel.setStyle("-fx-background-color: black;");
		previewLabel.setTextFill(item.color);
		previewLabel.setText(item.glyph);

		nameField.setText(item.name);
		textField.setText(item.glyph);
		colorBox.setValue(item.color);
		
		textField.textProperty().addListener((observable, oldValue, newValue) -> refresh());
		colorBox.valueProperty().addListener(value -> refresh());
	}
	
	/**
	 * Shows the item editor window.
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
		RItem rc = card.getResource();
		String name = nameField.getText();
		Color color = colorBox.getValue();
		String glyph = textField.getText();
		// check if anything was actually changed
		if (!name.equals(rc.name) || !glyph.equals(rc.glyph) || !color.equals(rc.color)) {
			card.setRedefined(card.isOriginal() ? true : false);
			name = name.isEmpty() ? card.toString() : name;
			RItem item = new RItem(card.toString(), name, glyph, color);
			bus.post(new SaveEvent.Resources(item));
			card.setChanged(true);				
		}
	}
	
	@FXML private void okPressed(ActionEvent event) throws ResourceException {
		applyPressed(event);
		cancelPressed(event);
	}
	
	@FXML private void helpPressed(ActionEvent event) {
		new HelpWindow().show("items.html");
	}
}
