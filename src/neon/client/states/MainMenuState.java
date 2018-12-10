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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import neon.client.help.HelpWindow;
import neon.client.ui.UserInterface;
import neon.common.console.Console;
import neon.common.event.ConfigurationEvent;
import neon.common.event.InputEvent;

/**
 * The state for the main menu screen in the game.
 * 
 * @author mdriesen
 *
 */
public final class MainMenuState extends State {
	private static final Logger logger = Logger.getGlobal();

	@FXML private Hyperlink newLink, loadLink, optionLink, quitLink;
	@FXML private Label versionLabel, titleLabel, subtitleLabel;

	private final UserInterface ui;
	private final EventBus bus;
	private Scene scene;
	
	/**
	 * Initializes this state.
	 * 
	 * @param ui
	 * @param version
	 * @param bus
	 */
	public MainMenuState(UserInterface ui, String version, EventBus bus) {
		this.ui = Objects.requireNonNull(ui, "user interface");
		this.bus = Objects.requireNonNull(bus, "event bus");
		
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
		quitLink.setOnAction(event -> bus.post(new InputEvent.Quit()));
		// also quit when pressing esc
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), quitLink::fire);
		
		// other key bindings
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> new Console(bus).show());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> new HelpWindow().show("main.html"));
		
		versionLabel.setText("release " + version);
	}
	
	@Override
	public void enter(TransitionEvent event) {
		logger.finest("entering main menu module");
		bus.register(this);
		ui.showScene(scene);
	}

	@Override
	public void exit(TransitionEvent event) { 
		logger.finest("exiting main menu module");
		bus.unregister(this);
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
			bus.post(new InputEvent.Quit());
		}
	}
	
	/**
	 * Configures the scene of the main menu state.
	 * 
	 * @param event
	 */
	@Subscribe
	private void configure(ConfigurationEvent event) {
		titleLabel.setText(event.title);
		subtitleLabel.setText(event.subtitle);
	}	
}
