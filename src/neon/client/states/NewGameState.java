/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import neon.client.help.HelpWindow;
import neon.client.ui.UserInterface;
import neon.common.event.ClientConfigurationEvent;
import neon.common.event.NewGameEvent;
import neon.common.resources.RCreature;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.util.GraphicsUtils;

public final class NewGameState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private Button cancelButton, startButton;
	@FXML private ListView<RCreature> speciesList;
	@FXML private ToggleGroup genderGroup;
	@FXML private TextField nameField;
	@FXML private Label instructionLabel, statsLabel, weightLabel, healthLabel, manaLabel;
	@FXML private Label descriptionLabel;
	@FXML private Spinner<Integer> strengthSpinner, constitutionSpinner, dexteritySpinner;
	@FXML private Spinner<Integer> intelligenceSpinner, wisdomSpinner, charismaSpinner;
	
	private final UserInterface ui;
	private final EventBus bus;
	private final ResourceManager resources;
	private final IntegerSpinnerValueFactory strFactory, conFactory, dexFactory, intFactory, wisFactory, chaFactory;
	private final int[] modifiers = {0, 1, 2, 4, 6, 8, 11, 14, 17, 21, 25, 29, 34, 39, 44, 50, 56, 62};
	private final int points = 20;
	
	private Scene scene;
	private int strMod = 0;
	private int conMod = 0;
	private int dexMod = 0;
	private int intMod = 0;
	private int wisMod = 0;
	private int chaMod = 0;
	private int pointsLeft = points;

	public NewGameState(UserInterface ui, EventBus bus, ResourceManager resources) {
		this.ui = ui;
		this.bus = bus;
		this.resources = resources;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/NewGame.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("failed to load new game menu: " + e.getMessage());
		}
		
		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());

		statsLabel.setText(points + " ability points to spend.");
		
		// list catches the esc, enter and F2 keys, we need a separate listener
		speciesList.setOnKeyPressed(event -> listKeyPressed(event));
		// text field catches the F2 key, another listener
		nameField.setOnKeyPressed(event -> fieldKeyPressed(event));		
		
		// TODO: list scrollt niet correct naar laatste item
		speciesList.setCellFactory(speciesList -> new CreatureCell());
		speciesList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> changeSpecies(oldValue, newValue));
		
		strFactory = new IntegerSpinnerValueFactory(3, 18, 10);
		conFactory = new IntegerSpinnerValueFactory(3, 18, 10);
		dexFactory = new IntegerSpinnerValueFactory(3, 18, 10);
		intFactory = new IntegerSpinnerValueFactory(3, 18, 10);
		wisFactory = new IntegerSpinnerValueFactory(3, 18, 10);
		chaFactory = new IntegerSpinnerValueFactory(3, 18, 10);
		
		strengthSpinner.setValueFactory(strFactory);
		constitutionSpinner.setValueFactory(conFactory);
		dexteritySpinner.setValueFactory(dexFactory);
		intelligenceSpinner.setValueFactory(intFactory);
		wisdomSpinner.setValueFactory(wisFactory);
		charismaSpinner.setValueFactory(chaFactory);

		strengthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> changeStr(oldValue, newValue));
		constitutionSpinner.valueProperty().addListener((obs, oldValue, newValue) -> changeCon(oldValue, newValue));
		dexteritySpinner.valueProperty().addListener((obs, oldValue, newValue) -> changeDex(oldValue, newValue));
		intelligenceSpinner.valueProperty().addListener((obs, oldValue, newValue) -> changeInt(oldValue, newValue));
		wisdomSpinner.valueProperty().addListener((obs, oldValue, newValue) -> changeWis(oldValue, newValue));
		charismaSpinner.valueProperty().addListener((obs, oldValue, newValue) -> changeCha(oldValue, newValue));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering new game state");
		ui.showScene(scene);
		nameField.requestFocus();
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting new game state");
	}
	
	private void listKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ESCAPE)) {
			bus.post(new TransitionEvent("cancel"));
		} else if (event.getCode().equals(KeyCode.ENTER)) {
			startGame();
		} else if (event.getCode().equals(KeyCode.F2)) {
			showHelp();
		}
	}
	
	private void fieldKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.F2)) {
			showHelp();
		}
	}
	
	@FXML private void showHelp() {
		new HelpWindow().show("load.html");
	}
	
	@FXML private void startGame() {
		// collect player character data
		String name = nameField.getText();
		String species = speciesList.getSelectionModel().getSelectedItem().id;
		String gender = genderGroup.getSelectedToggle().getUserData().toString();
		
		int strength = strengthSpinner.getValue();
		int constitution = constitutionSpinner.getValue();
		int dexterity = dexteritySpinner.getValue();
		int intelligence = intelligenceSpinner.getValue();
		int wisdom = wisdomSpinner.getValue();
		int charisma = charismaSpinner.getValue();
		
		if (name.isEmpty()) {
			ui.showMessage("Enter a valid character name.", 1000);
//		} else if (pointsLeft > 0) {
//			ui.showMessage("You have unspent ability points.", 1000);			
//		} else if (pointsLeft < 0) {
//			ui.showMessage("You have spent too many ability points.", 1000);			
		} else {
			// let the server know that the game module is waiting for game data
			bus.post(new NewGameEvent.Check(name, species, gender, strength, constitution, dexterity, intelligence, wisdom, charisma));
		}
	}
	
	@Subscribe
	private void onCreationPass(NewGameEvent.Pass event) {
		// transition to the actual game module
		bus.post(new TransitionEvent("start game"));		
	}
	
	@Subscribe
	private void onCreationFail(NewGameEvent.Fail event) {
		ui.showMessage("The character you created is not valid.", 1000);			
	}
	
	/**
	 * Configures the scene of this main menu module.
	 * 
	 * @param event
	 * @throws ResourceException 
	 */
	@Subscribe
	private void onConfigure(ClientConfigurationEvent event) throws ResourceException {
		for (String id : event.getPlayableSpecies()) {
			speciesList.getItems().add(resources.getResource("creatures", id));
		}
		
		speciesList.getSelectionModel().select(0);
		
		int strength = speciesList.getSelectionModel().getSelectedItem().strength;
		weightLabel.setText("Carry weight: " + 6*strength + "/" + 9*strength);
		int constitution = speciesList.getSelectionModel().getSelectedItem().constitution;
		healthLabel.setText("Health: " + 3*constitution + " HP");
		int intelligence = speciesList.getSelectionModel().getSelectedItem().intelligence;
		manaLabel.setText("Mana: " + 6*intelligence);
	}
	
	private void changeStr(int oldValue, int newValue) {
		weightLabel.setText("Carry weight: " + 6*newValue + "/" + 9*newValue);
		RCreature species = speciesList.getSelectionModel().getSelectedItem();
		strMod = newValue - species.strength;
		changeAbility();
	}
	
	private void changeCon(int oldValue, int newValue) {
		healthLabel.setText("Health: " + 3*newValue + " HP");
		RCreature species = speciesList.getSelectionModel().getSelectedItem();
		conMod =  newValue - species.constitution;
		changeAbility();
	}
	
	private void changeDex(int oldValue, int newValue) {
		RCreature species = speciesList.getSelectionModel().getSelectedItem();
		dexMod = newValue - species.dexterity;
		changeAbility();
	}
	
	private void changeInt(int oldValue, int newValue) {
		manaLabel.setText("Mana: " + 6*newValue);
		RCreature species = speciesList.getSelectionModel().getSelectedItem();
		intMod = newValue - species.intelligence;
		changeAbility();
	}
	
	private void changeWis(int oldValue, int newValue) {
		RCreature species = speciesList.getSelectionModel().getSelectedItem();
		wisMod = newValue - species.wisdom;
		changeAbility();
	}
	
	private void changeCha(int oldValue, int newValue) {
		RCreature species = speciesList.getSelectionModel().getSelectedItem();
		chaMod = newValue - species.charisma;
		changeAbility();
	}
	
	private void changeAbility() {
		pointsLeft = points;
		pointsLeft -= strMod < 0 ? strMod : modifiers[strMod];
		pointsLeft -= conMod < 0 ? conMod : modifiers[conMod];
		pointsLeft -= dexMod < 0 ? dexMod : modifiers[dexMod];
		pointsLeft -= intMod < 0 ? intMod : modifiers[intMod];
		pointsLeft -= wisMod < 0 ? wisMod : modifiers[wisMod];
		pointsLeft -= chaMod < 0 ? chaMod : modifiers[chaMod];
		
		if (pointsLeft >= 0) {
			statsLabel.setTextFill(Color.WHITE);
			statsLabel.setText(pointsLeft + " ability points to spend.");
		} else {
			statsLabel.setTextFill(Color.RED);
			statsLabel.setText(-pointsLeft + " ability points to remove!");
		}
		
		RCreature species = speciesList.getSelectionModel().getSelectedItem();

		if ((pointsLeft == 2 && strMod > 4) || (pointsLeft == 1 && strMod > 1) || (pointsLeft <= 0)) {
			strFactory.setMax(strengthSpinner.getValue());
		} else {
			strFactory.setMax(species.strength + 8);			
		}

		if ((pointsLeft == 2 && conMod > 4) || (pointsLeft == 1 && conMod > 1) || (pointsLeft <= 0)) {
			conFactory.setMax(constitutionSpinner.getValue());
		} else {
			conFactory.setMax(species.constitution + 8);
		}
		
		if ((pointsLeft == 2 && dexMod > 4) || (pointsLeft == 1 && dexMod > 1) || (pointsLeft <= 0)) {
			dexFactory.setMax(dexteritySpinner.getValue());
		} else {
			dexFactory.setMax(species.dexterity + 8);
		}
		
		if ((pointsLeft == 2 && intMod > 4) || (pointsLeft == 1 && intMod > 1) || (pointsLeft <= 0)) {
			intFactory.setMax(intelligenceSpinner.getValue());
		} else {
			intFactory.setMax(species.intelligence + 8);
		}
		
		if ((pointsLeft == 2 && wisMod > 4) || (pointsLeft == 1 && wisMod > 1) || (pointsLeft <= 0)) {
			wisFactory.setMax(wisdomSpinner.getValue());
		} else {
			wisFactory.setMax(species.wisdom + 8);
		}
		
		if ((pointsLeft == 2 && chaMod > 4) || (pointsLeft == 1 && chaMod > 1) || (pointsLeft <= 0)) {
			chaFactory.setMax(charismaSpinner.getValue());
		} else {
			chaFactory.setMax(species.charisma + 8);			
		}
	}
	
	private void changeSpecies(RCreature oldValue, RCreature newValue) {
		if (newValue != null) {
			strengthSpinner.getValueFactory().setValue(newValue.strength + strMod);
			constitutionSpinner.getValueFactory().setValue(newValue.constitution + conMod);
			dexteritySpinner.getValueFactory().setValue(newValue.dexterity + dexMod);
			intelligenceSpinner.getValueFactory().setValue(newValue.intelligence + intMod);
			wisdomSpinner.getValueFactory().setValue(newValue.wisdom + wisMod);
			charismaSpinner.getValueFactory().setValue(newValue.charisma + chaMod);
			
			descriptionLabel.setText(newValue.description);
		}
	}

   private class CreatureCell extends ListCell<RCreature> {
    	@Override
    	public void updateItem(RCreature creature, boolean empty) {
    		super.updateItem(creature, empty);
    		if (empty || creature == null) {
    			setGraphic(null);
    			setText(null);
    		} else {
    			setStyle("-fx-text-fill: " + GraphicsUtils.getColorString(creature.color));
    			setText(creature.name);
    		}
    	}
    }
}
