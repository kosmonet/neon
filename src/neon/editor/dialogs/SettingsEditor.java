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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import neon.system.resources.RModule;

public class SettingsEditor {
	private static final Logger logger = Logger.getGlobal();

	@FXML private Label instructionLabel;
	@FXML private TextField titleField;
	@FXML private ListView<String> speciesList;
	
	private final Stage stage;
	private Scene scene;
	
	public SettingsEditor(RModule module, Stage mainStage) {
		stage = new Stage();
		stage.initOwner(mainStage);
		stage.setTitle("Module settings");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../editor.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load settings editor ui");
		}
		
		titleField.setText(module.getTitle());
		speciesList.getItems().addAll(module.getPlayableSpecies());
		
	}
	
	public void show() {
		stage.showAndWait();
	}
}
