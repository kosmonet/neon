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
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import neon.client.UserInterface;
import neon.client.ui.DescriptionLabel;
import neon.common.resources.RCreature;
import neon.entity.components.Graphics;
import neon.entity.components.Info;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;

/**
 * 
 * @author mdriesen
 *
 */
public class JournalState extends State {
	private static final Logger logger = Logger.getGlobal();
	
	@FXML private Button cancelButton;
	@FXML private DescriptionLabel description;
	@FXML private Label infoLabel;
	@FXML private Label speedLabel, strengthLabel, constitutionLabel, dexterityLabel;
	@FXML private Label intelligenceLabel, wisdomLabel, charismaLabel;

	private final UserInterface ui;
	private Scene scene;

	public JournalState(UserInterface ui, EventBus bus) {
		this.ui = ui;
		
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
		
    	Info info = event.getParameter(Info.class);
    	Stats stats = event.getParameter(Stats.class);
    	Graphics graphics = event.getParameter(Graphics.class);
    	Creature.Resource resource = event.getParameter(Creature.Resource.class);
    	
    	description.update(resource.getResource().name, graphics);
    	RCreature species = stats.getSpecies();
    	infoLabel.setText(info.getName() + ", " + info.getGender() + " " + species.name);
    	speedLabel.setText("Speed: " + species.speed);
    	
    	strengthLabel.setText("Strength: " + stats.getBaseStr());
    	constitutionLabel.setText("Constitution: " + stats.getBaseCon());
    	dexterityLabel.setText("Dexterity: " + stats.getBaseDex());
    	intelligenceLabel.setText("Intelligence: " + stats.getBaseInt());
    	wisdomLabel.setText("Wisdom: " + stats.getBaseWis());
    	charismaLabel.setText("Charisma: " + stats.getBaseCha());
    	
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting journal state");
	}
}
