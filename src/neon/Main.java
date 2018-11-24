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

package neon;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;

import neon.client.Client;
import neon.common.logging.NeonLogFormatter;
import neon.common.net.ClientSocket;
import neon.common.net.ServerSocket;
import neon.server.Server;

/**
 * Application main class sets up the server and client and connects them to
 * exchange messages.
 * 
 * @author mdriesen
 *
 */
public class Main extends Application {
	private static final String version = "0.5.0";	// current version
	private static final Logger logger = Logger.getGlobal();

	@Override
	public void start(Stage primaryStage) {
		// create sockets to connect client and server
		ServerSocket ss = new ServerSocket("Server Socket");
		ClientSocket cs = new ClientSocket("Client Socket");
		ss.connect(cs);
		
		// server runs on its own thread
		Server server = new Server(version, ss);
		Thread serverThread = new Thread(server, "Server Thread");
		// make sure the server thread doesn't prevent the engine from shutting down
		serverThread.setDaemon(true);
		serverThread.start();

		// client uses a separate thread for messaging with the server 
		Client client = new Client(version, cs, primaryStage);
		Thread clientThread = new Thread(client, "Client Thread");
		// make sure the client thread doesn't prevent the engine from shutting down
		clientThread.setDaemon(true);
		clientThread.start();
	}

	public static void main(String[] args) {
		// set up the logger
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		handler.setFormatter(new NeonLogFormatter());
		logger.addHandler(handler);

		launch(args);
	}
}
