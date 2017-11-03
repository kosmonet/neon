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

package neon.client.modules;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import neon.client.UserInterface;
import neon.common.console.Console;
import neon.common.event.ClientConfigurationEvent;
import neon.common.event.QuitEvent;

/**
 * The module for the main menu screen in the game.
 * 
 * @author mdriesen
 *
 */
public class MainMenuModule extends Module {
	private static final Logger logger = Logger.getGlobal();

	@FXML private Hyperlink newLink, loadLink, optionLink, quitLink;
	@FXML private Label versionLabel, titleLabel;

	private final UserInterface ui;
	private final EventBus bus;
	private Scene scene;
	
	/**
	 * Initializes this module.
	 * 
	 * @param ui
	 * @param version
	 * @param bus
	 */
	public MainMenuModule(UserInterface ui, String version, EventBus bus) {
		this.ui = ui;
		this.bus = bus;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/neon/client/scenes/MainMenu.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/neon/client/scenes/main.css").toExternalForm());
		} catch (IOException e) {
			logger.severe("failed to load main menu " + e.getMessage());
		}
		
		newLink.setOnMouseEntered(event -> newLink.requestFocus());
		newLink.setOnAction(event -> bus.post(new TransitionEvent("new game")));
        
		loadLink.setOnMouseEntered(event -> loadLink.requestFocus());
		loadLink.setOnAction(event -> bus.post(new TransitionEvent("load game")));

		optionLink.setOnMouseEntered(event -> optionLink.requestFocus());
		optionLink.setOnAction(event -> bus.post(new TransitionEvent("options")));

		quitLink.setOnMouseEntered(event -> quitLink.requestFocus());
		quitLink.setOnAction(event -> Platform.exit());
		// also quit when pressing esc
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> quitLink.fire());
		
		// show console when pressing F1
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> new Console(bus).show());
		
		versionLabel.setText("release " + version);
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering main menu module");
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) { 
		logger.finest("exiting main menu module");
	}
	
	@FXML private void loadWebsite() throws IOException, URISyntaxException {
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("https://github.com/kosmonet/"));
		}		
	}
	
	@FXML private void newKeyPressed(KeyEvent event) {
		// default action key is space, so we need an extra key handler for enter
		if (event.getCode().equals(KeyCode.ENTER)) {
			bus.post(new TransitionEvent("new game"));
		}
	}
	
	@FXML private void loadKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER)) {
			bus.post(new TransitionEvent("load game"));
		}
	}
	
	@FXML private void optionKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER)) {
			bus.post(new TransitionEvent("options"));
		}
	}
	
	@FXML private void quitKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER)) {
			bus.post(new QuitEvent());
		}
	}
	
	/**
	 * Configures the scene of this main menu module.
	 * 
	 * @param event
	 */
	@Subscribe
	public void configure(ClientConfigurationEvent event) {
		titleLabel.setText(event.getTitle());
	}	
}
