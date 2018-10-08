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

package neon.client.states;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import neon.client.UserInterface;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.common.event.ConversationEvent;
import neon.entity.components.Graphics;
import neon.entity.entities.Creature;

public class ConversationState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final UserInterface ui;
	
	@FXML private TextFlow flow;
	@FXML private Button cancelButton;
	@FXML private DescriptionLabel description;
	@FXML private VBox subjects;
	@FXML private ScrollPane scroller;
	
	private Scene scene;
	private int index;
	
	public ConversationState(UserInterface ui, EventBus bus) {
		this.bus = bus;
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Conversation.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load conversation interface: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> showHelp());
		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		scroller.setOnKeyPressed(event -> scrollKeyPressed(event));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering conversation module");
    	Graphics graphics = event.getParameter(Graphics.class);
    	Creature.Resource resource = event.getParameter(Creature.Resource.class);
		bus.post(new ConversationEvent.Start(PLAYER_UID, graphics.getEntity()));		
		flow.getChildren().clear();
		description.update(resource.getResource().name, graphics);
		ui.showScene(scene);
	}

	@Subscribe
	public void update(ConversationEvent.Update event) {
		flow.getChildren().add(new Text(event.getAnswer()));
		
		subjects.getChildren().clear();
		
		for (Entry<String, String> topic : event.getTopics().entrySet()) {
			Hyperlink link = new Hyperlink(topic.getValue());
			subjects.getChildren().add(link);
			link.setOnAction(action -> bus.post(new ConversationEvent.Answer(topic.getKey())));			
		}
		
		index = 0;
		Platform.runLater(subjects.getChildren().get(0)::requestFocus);
		Platform.runLater(() -> scroller.setVvalue(scroller.getVmax()));
	}
	
	/**
	 * Makes the scroll pane and list of subjects scroll in an orderly manner.
	 * 
	 * @param event
	 */
	private void scrollKeyPressed(KeyEvent event) {
		// scroll the subjects 1/30th of the scrollpane height (approximately one line)
		double step = scroller.getHeight()/(30*(flow.getHeight() - scroller.getHeight()));
		
		switch (event.getCode()) {
		case DOWN:
			if (scroller.getVvalue() < scroller.getVmax()) {
				scroller.setVvalue(scroller.getVvalue() + step);
			} else {
				index = Math.min(index + 1, subjects.getChildren().size() - 1);
				subjects.getChildren().get(index).requestFocus();				
			}
			event.consume();
			break;
		case UP: 
			if (index <= 0) {
				scroller.setVvalue(scroller.getVvalue() - step);
			} else {
				index = Math.max(index - 1, 0);
				subjects.getChildren().get(index).requestFocus();
			}
			event.consume();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting conversation module");		
	}
	
	@FXML private void showHelp() {
		new HelpWindow().show("conversation.html");
	}
}
