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

package neon.client.console;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import neon.system.event.ScriptEvent;
import neon.system.logging.NeonLogFormatter;

/**
 * A debug console for the neon engine. 
 * 
 * @author mdriesen
 *
 */
public class Console {
	private static final Logger logger = Logger.getGlobal();

	@FXML private TextFlow flow;
	@FXML private ScrollPane scroller;
	
	private final Stage stage = new Stage();
	private final EventBus bus;
	private StringBuilder builder = new StringBuilder(">>_\n");
	private Text text = new Text(builder.toString());
	private Scene scene;
	private boolean wait = false;
	private int offset = 1;
	
	/**
	 * Initializes this console with an {@code EventBus}.
	 * 
	 * @param bus
	 */
	public Console(EventBus bus) {
		this.bus = bus;
		bus.register(this);
		stage.setTitle("Console");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Console.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("../scenes/main.css").toExternalForm());
			stage.setScene(scene);
		} catch (IOException e) {
			logger.severe("failed to load console: " + e.getMessage());
		}
	
		flow.getChildren().addListener(new ScrollListener());
		text.setFill(Color.WHITE);
		flow.getChildren().add(text);

		// two different handlers, keyTyped can't handle the enter key
		scene.setOnKeyPressed(event -> keyPressed(event));
		scene.setOnKeyTyped(event -> keyTyped(event));
		// close console when pressing esc
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> close());

		logger.addHandler(new ConsoleHandler(Level.ALL));
	}
	
	/**
	 * Shows this console on screen.
	 */
	public void show() {
		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();
	}
	
	private void close() {
		bus.unregister(this);
		stage.close();
	}
	
	/**
	 * Prints a message on the console.
	 * 
	 * @param event a {@code ConsoleEvent} with the message to be printed
	 */
	@Subscribe
	public void print(ConsoleEvent event) {
		Text text = new Text(event.getMessage() + "\n");
		text.setFill(Color.BEIGE);
		flow.getChildren().add(flow.getChildren().size() - offset, text);
	}
	
	private void keyPressed(KeyEvent event) {
		switch (event.getCode()) {
		case ENTER:
			// wait flag to prevent keyTyped from printing enter and backspace characters
			wait = true;
			// new lines should appear at a different offset in the TextFlow while switching to new Text node
			offset = 0;
			bus.post(new ScriptEvent(builder.substring(2, builder.length() - 2)));
			text.setText(builder.deleteCharAt(builder.length() - 2).toString());
			builder = new StringBuilder(">>_\n");
			text = new Text(builder.toString());
			text.setFill(Color.WHITE);
			flow.getChildren().add(text);
			offset = 1;
			break;
		case BACK_SPACE:
			wait = true;
			builder.deleteCharAt(builder.length() - 3);
			text.setText(builder.toString());
			break;
		default:
			break;
		}
	}

	private void keyTyped(KeyEvent event) {
		if (!wait) {
			String character = event.getCharacter();
			builder.insert(builder.length() - 2, character);
			text.setText(builder.toString());
		} else {
			wait = false;
		}
	}
	
	private class ScrollListener implements ListChangeListener<Node> {
		@Override
		public void onChanged(ListChangeListener.Change<? extends Node> change) {
			flow.layout();
			scroller.layout();
			scroller.setVvalue(1.0f);
		}
	}
	
	private class ConsoleHandler extends Handler {
		private ConsoleHandler(Level level) {
			setLevel(level);
			setFormatter(new NeonLogFormatter());
		}
		
		@Override
		public void close() throws SecurityException {}

		@Override
		public void flush() {}

		@Override
		public void publish(LogRecord record) {
			Text text = new Text(getFormatter().format(record));
			text.setFill(Color.MOCCASIN);
			flow.getChildren().add(flow.getChildren().size() - offset, text);
		}
	}
}
