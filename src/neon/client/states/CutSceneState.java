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
import javafx.scene.web.WebView;
import neon.client.ui.UserInterface;
import neon.common.resources.CClient;
import neon.common.resources.RText;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

public final class CutSceneState extends State {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final EventBus bus;
	private final UserInterface ui;
	private final ResourceManager resources;

	@FXML private Button continueButton;
	@FXML private WebView view;

	private Scene scene;

	public CutSceneState(UserInterface ui, EventBus bus, ResourceManager resources) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/Scene.fxml"));
		loader.setController(this);

		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			LOGGER.severe("failed to load inventory interface: " + e.getMessage());
		}

		continueButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
		view.getEngine().setUserStyleSheetLocation(getClass().getResource("../help/help.css").toString());
	}
	
	@Override
	public void enter(TransitionEvent event) {
		LOGGER.finest("entering cutscene state");
		try {
			CClient config = resources.getResource("config", "client");
			if(config.intro.isEmpty()) {
				bus.post(new TransitionEvent("cancel"));
			} else {
				LOGGER.info("loading cutscene text: " + config.intro);
				RText text = resources.getResource("texts", config.intro);
				view.getEngine().loadContent(text.text);
				ui.showScene(scene);
			}
		} catch (ResourceException e) {
			bus.post(new TransitionEvent("cancel"));
			LOGGER.severe("unable to load cutscene text");
		}
	}

	@Override
	public void exit(TransitionEvent event) {
		LOGGER.finest("exiting cutscene state");
	}
}
