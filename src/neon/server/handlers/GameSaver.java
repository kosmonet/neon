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

package neon.server.handlers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.PlayerInfo;
import neon.common.event.InputEvent;
import neon.common.files.FileUtils;
import neon.common.resources.CGame;
import neon.common.resources.ResourceManager;
import neon.server.Configuration;
import neon.server.entity.EntityManager;

/**
 * Class that handles saving games.
 * 
 * @author mdriesen
 *
 */
public class GameSaver {
	private static final Logger LOGGER = Logger.getGlobal();

	private final EntityManager entities;
	private final ResourceManager resources;
	private final Configuration configuration;
	
	/**
	 * Resource manager, entity manager and configuration must not be null.
	 * 
	 * @param resources
	 * @param entities
	 * @param configuration
	 */
	public GameSaver(ResourceManager resources, EntityManager entities, Configuration configuration) {
		this.entities = Objects.requireNonNull(entities, "entity manager");
		this.configuration = Objects.requireNonNull(configuration, "configuration");
		this.resources = Objects.requireNonNull(resources, "resource manager");
	}
	
	/**
	 * Saves the currently running game when a save event is received.
	 * 
	 * @param event
	 * @throws IOException	if the configuration can't be saved
	 */
	@Subscribe
	private void onSaveGame(InputEvent.Save event) throws IOException {
		LOGGER.info("saving game");
		
		// store all cached entities
		entities.flushEntities();
		// store all cached maps
		entities.flushMaps();		
		// save configuration (current map, TODO: calendar)
		CGame game = new CGame(configuration.getCurrentMap().getID(), 0, 0, 0, Collections.emptyList(), Collections.emptySet());
		resources.addResource(game);
		
		// move the temp folder to the saves folder
		Entity player = entities.getEntity(Configuration.PLAYER_UID);
		PlayerInfo info = player.getComponent(PlayerInfo.class);
		FileUtils.moveFolder(Paths.get("temp"), Paths.get("saves", info.getName()));
	}
}
