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
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import neon.editor.Card;
import neon.editor.SaveEvent;
import neon.system.resources.RCreature;
import neon.system.resources.ResourceException;

/**
 * This resource editor shows a modal dialog window to edit the properties
 * of a creature:
 * <ul>
 * 	<li>the display name</li>
 * </ul>
 * 
 * The creature editor does not perform the actual saving of the edited 
 * creature resource. Instead, it sends a {@code SaveEvent} to request 
 * that the creature resource be saved.
 * 
 * @author mdriesen
 *
 */
public class CreatureEditor {
	private static final Logger logger = Logger.getGlobal();

//	@FXML private Label instructionLabel;
	@FXML private TextField nameField;
	
	private final Stage stage = new Stage();
	private final EventBus bus;
	private final String id;
	private final Card card;
	private Scene scene;
	
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
		id = creature.getID();
		this.bus = bus;
		
		stage.setTitle("Creature properties");
		stage.initModality(Modality.APPLICATION_MODAL); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Creature.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load creature editor ui");
		}
		
		nameField.setText(creature.getName());
//		instructionLabel.setText("The creature name will be displayed in-game, not the id.");
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
	
	@FXML private void applyPressed(ActionEvent event) {
		// save changes to a new creature resource
		RCreature creature = new RCreature(id, nameField.getText());
		bus.post(new SaveEvent("creatures", creature));
		card.setChanged(true);
	}
	
	@FXML private void okPressed(ActionEvent event) {
		applyPressed(event);
		cancelPressed(event);
	}
}
