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

package neon.client.handlers;

import java.io.IOException;

import com.google.common.eventbus.Subscribe;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.Map;
import neon.client.ui.UserInterface;
import neon.common.entity.components.Shape;
import neon.common.event.UpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.systems.time.RestEvent;

public class GameHandler {
	private static final long PLAYER_UID = 0;
	
	private final UserInterface ui;
	private final ResourceManager resources;
	private final ComponentManager components;
	private final Configuration config;
	private final NeonFileSystem files;
	
	public GameHandler(UserInterface ui, NeonFileSystem files, ComponentManager components, ResourceManager resources, Configuration config) {
		this.ui = ui;
		this.files = files;
		this.components = components;
		this.resources = resources;
		this.config = config;
	}
	
	@Subscribe
	private void onMapChange(UpdateEvent.Map event) throws ResourceException, IOException {
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		RMap resource = resources.getResource("maps", event.id);
		Map map = new Map(resource, files);
		map.addEntity(PLAYER_UID, shape.getX(), shape.getY());
		config.setCurrentMap(map);
	}
	
	@Subscribe
	private void onSleep(RestEvent.Wake event) {
		FadeTransition transition = new FadeTransition(Duration.millis(2000), ui.getCurrentScene().getRoot());
		transition.setFromValue(1.0);
	    transition.setToValue(0.0);
	    transition.setAutoReverse(true);
	    transition.setCycleCount(2);
	    transition.setOnFinished(action -> ui.showOverlayMessage("You have slept.", 1500));
	    transition.play();
	}
}
