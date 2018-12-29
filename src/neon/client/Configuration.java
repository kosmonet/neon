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

package neon.client;

import java.util.Objects;

/**
 * Configuration for the client.
 * 
 * @author mdriesen
 *
 */
public class Configuration {
	/** The uid of the player. */
	public static final long PLAYER_UID = 0;
	
	private boolean paused = true;
	private Map map;
	
	/**
	 * Sets the map to be displayed. The map must not be null.
	 * 
	 * @param map
	 */
	public void setCurrentMap(Map map) {
		this.map = Objects.requireNonNull(map, "map");
	}
	
	/**
	 * Gets the map to be displayed.
	 * 
	 * @return
	 */
	public Map getCurrentMap() {
		return map;
	}
	
	/**
	 * Pauses the game.
	 */
	public void pause() {
		paused = true;
	}
	
	/**
	 * Unpauses the game.
	 */
	public void unpause() {
		paused = false;
	}
	
	/**
	 * Checks whether the game is paused or not.
	 * 
	 * @return
	 */
	public boolean isPaused() {
		return paused;
	}
}
