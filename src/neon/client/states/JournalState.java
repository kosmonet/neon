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
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import neon.client.ClientUtils;
import neon.client.ComponentManager;
import neon.client.ui.DescriptionLabel;
import neon.client.ui.UserInterface;
import neon.common.entity.Skill;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Skills;
import neon.common.entity.components.Stats;

/**
 * 
 * @author mdriesen
 *
 */
public final class JournalState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	@FXML private Button cancelButton;
	@FXML private DescriptionLabel description;
	@FXML private Label infoLabel, healthLabel, manaLabel, weightLabel, levelLabel;
	@FXML private Label speedLabel, strengthLabel, constitutionLabel, dexterityLabel;
	@FXML private Label intelligenceLabel, wisdomLabel, charismaLabel, staminaLabel;
	@FXML private VBox skillBox;

	private final UserInterface ui;
	private final ComponentManager components;
	private Scene scene;

	public JournalState(UserInterface ui, EventBus bus, ComponentManager components) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.components = Objects.requireNonNull(components, "component manager");
		Objects.requireNonNull(bus, "event bus");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Journal.fxml"));
		loader.setController(this);

		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load journal: " + e.getMessage());
		}

		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering journal state");
		
		Stats stats = components.getComponent(PLAYER_UID, Stats.class);
		PlayerInfo playerInfo = components.getComponent(PLAYER_UID, PlayerInfo.class);
		CreatureInfo creatureInfo = components.getComponent(PLAYER_UID, CreatureInfo.class);
		Skills skills = components.getComponent(PLAYER_UID, Skills.class);
	
		description.updateCreature(components.getComponents(PLAYER_UID));
    	infoLabel.setText(playerInfo.getName() + ", " + playerInfo.getGender() + " " + creatureInfo.getName());
    	speedLabel.setText("Speed: " + stats.getSpeed());
    	levelLabel.setText("Level " + stats.getLevel() + " (" + skills.getSkillIncreases() + "/10)");
    	
    	strengthLabel.setText("Strength: " + stats.getBaseStr());
    	constitutionLabel.setText("Constitution: " + stats.getBaseCon());
    	dexterityLabel.setText("Dexterity: " + stats.getBaseDex());
    	intelligenceLabel.setText("Intelligence: " + stats.getBaseInt());
    	wisdomLabel.setText("Wisdom: " + stats.getBaseWis());
    	charismaLabel.setText("Charisma: " + stats.getBaseCha());
    	
    	Inventory inventory = components.getComponent(PLAYER_UID, Inventory.class);
    	int weight = ClientUtils.getWeight(inventory, components);
    	weightLabel.setText("Encumbrance: " + weight + " of " + 6*stats.getBaseStr()+ "/" + 9*stats.getBaseStr() + " stone");

    	healthLabel.setText("Health: " + stats.getHealth() + "/" + stats.getBaseHealth());
		if (stats.getHealth()/stats.getBaseHealth() < 0.1) {
			healthLabel.setTextFill(Color.RED);
		} else {
			healthLabel.setTextFill(Color.WHITE);			
		}

		manaLabel.setText("Mana: " + stats.getMana() + "/" + stats.getBaseMana());
		if (stats.getMana()/stats.getBaseMana() < 0.1) {
			manaLabel.setTextFill(Color.RED);
		} else {
			manaLabel.setTextFill(Color.WHITE);			
		}
   	
		staminaLabel.setText("Stamina: " + stats.getStamina() + "/" + stats.getBaseStamina());
		if (stats.getStamina()/stats.getBaseStamina() < 0.1) {
			manaLabel.setTextFill(Color.RED);
		} else {
			manaLabel.setTextFill(Color.WHITE);			
		}
   	
		skillBox.getChildren().clear();
		for (Skill skill : Skill.values()) {
			skillBox.getChildren().add(new Label(skill + ": " + (int) skills.getSkill(skill)));
		}
    	
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting journal state");
	}
}
