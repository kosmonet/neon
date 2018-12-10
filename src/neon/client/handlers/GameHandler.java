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
import java.util.Objects;

import com.google.common.eventbus.Subscribe;

import neon.client.ComponentManager;
import neon.client.Configuration;
import neon.client.Map;
import neon.common.entity.components.Shape;
import neon.common.event.UpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

public class GameHandler {
	private static final long PLAYER_UID = 0;
	
	private final ResourceManager resources;
	private final ComponentManager components;
	private final Configuration config;
	private final NeonFileSystem files;
	
	public GameHandler(NeonFileSystem files, ComponentManager components, ResourceManager resources, Configuration config) {
		this.files = Objects.requireNonNull(files, "file system");
		this.components = Objects.requireNonNull(components, "component manager");
		this.resources = Objects.requireNonNull(resources, "resource manager");
		this.config = Objects.requireNonNull(config, "configuration");
	}
	
	@Subscribe
	private void onMapChange(UpdateEvent.Map event) throws ResourceException, IOException {
		Shape shape = components.getComponent(PLAYER_UID, Shape.class);
		RMap resource = resources.getResource("maps", event.id);
		Map map = new Map(resource, files);
		map.addEntity(PLAYER_UID, shape.getX(), shape.getY());
		config.setCurrentMap(map);
	}
}
