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
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

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
import neon.client.ComponentManager;
import neon.client.help.HelpWindow;
import neon.client.ui.DescriptionLabel;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Provider;
import neon.systems.conversation.ConversationEvent;
import neon.systems.conversation.Topic;

public final class ConversationState extends State {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final UserInterface ui;
	private final ComponentManager components;
	
	@FXML private TextFlow flow;
	@FXML private Button cancelButton;
	@FXML private DescriptionLabel description;
	@FXML private VBox subjects;
	@FXML private ScrollPane scroller;
	
	private Scene scene;
	private int index;
	
//	private String test = "Nihonium is a synthetic chemical element with symbol Nh and atomic number "
//			+ "113. It is extremely radioactive; its most stable known isotope, nihonium-286, has a "
//			+ "half-life of about 10 seconds. In the periodic table, nihonium is a transactinide element "
//			+ "at the intersection of period 7 and group 13. Its creation was reported in 2003 by a "
//			+ "Russian–American collaboration at the Joint Institute for Nuclear Research in Dubna, Russia, "
//			+ "and in 2004 by a team of Japanese scientists at Riken in Wakō, Japan. The discoveries were "
//			+ "confirmed by independent teams working in the United States, Germany, Sweden, and China. In "
//			+ "2015 the element was officially recognised; naming rights were assigned to Riken, as they "
//			+ "were judged to have been first to confirm their discovery. The name, approved in the same "
//			+ "year (announcement pictured), derives from a Japanese word for Japan, Nihon. Few details "
//			+ "are known about nihonium, as it has only been formed in very small amounts that decay away "
//			+ "within seconds.";
	
	public ConversationState(UserInterface ui, EventBus bus, ComponentManager components) {
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.components = Objects.requireNonNull(components, "component manager");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Conversation.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load conversation interface: " + e.getMessage());
		}
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::showHelp);
		cancelButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		scroller.setOnKeyPressed(this::scrollKeyPressed);
		// to make the scrollpane scroll all the way down when new topics are added
        flow.heightProperty().addListener(value -> scroller.vvalueProperty().setValue(1));
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering conversation module");
		bus.register(this);
		bus.post(new ConversationEvent.Start(PLAYER_UID, event.getParameter(Long.class)));		
		description.updateCreature(components.getComponents(event.getParameter(Long.class)));
		flow.getChildren().clear();	
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) {
		logger.finest("exiting conversation module");
		bus.unregister(this);
	}
	
	@Subscribe
	private void onConverationUpdate(ConversationEvent.Update event) {
		flow.getChildren().add(new Text("\n"));
		flow.getChildren().add(new Text(event.answer));
//		flow.getChildren().add(new Text(test));
//		flow.getChildren().add(new Text(test));
		subjects.getChildren().clear();
		
		for (Topic topic : event.getTopics()) {
			Hyperlink link = new Hyperlink(topic.text);
			subjects.getChildren().add(link);
			link.setOnAction(action -> ask(topic));			
		}
		
		if (components.hasComponent(event.listener, Provider.class)) {
			Provider provider = components.getComponent(event.listener, Provider.class);
			if (provider.hasService(Provider.Service.HEALER)) {
				Hyperlink link = new Hyperlink("Can you heal me?");
				link.getStyleClass().add("service");
				subjects.getChildren().add(link);
				link.setOnAction(action -> heal());			
			}

			if (provider.hasService(Provider.Service.TRADE)) {
				Hyperlink link = new Hyperlink("What have you got for sale?");
				link.getStyleClass().add("service");
				subjects.getChildren().add(link);
				link.setOnAction(action -> bus.post(new TransitionEvent("trade")));			
			}
		}
		
		index = 0;
		subjects.getChildren().get(0).requestFocus();
	}
	
	private void heal() {
		ui.showMessage("Another soul saved!", 1000);
	}
	
	@Subscribe 
	private void onConversationEnd(ConversationEvent.End event) {
		bus.post(new TransitionEvent("cancel"));
	}
	
	private void ask(Topic topic) {
		flow.getChildren().add(new Text("\n\t"));
		Text text = new Text(topic.text);
		text.setStyle("-fx-fill: indianred");
		flow.getChildren().add(text);
		bus.post(new ConversationEvent.Answer(topic.id));
	}
	
	/**
	 * Makes the scroll pane and list of subjects scroll in an orderly manner.
	 * 
	 * @param event
	 */
	private void scrollKeyPressed(KeyEvent event) {		
		// scroll the subjects 1/30th of the scrollpane height (approximately one line)
		double step = scroller.getHeight()/(30*(flow.getHeight() - scroller.getHeight()));
//		System.out.println(scroller.getVvalue() + "/" + scroller.getVmax());
		
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
	
	@FXML private void showHelp() {
		new HelpWindow().show("conversation.html");
	}
}
