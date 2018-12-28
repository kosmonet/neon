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

package neon.server;

import java.util.Objects;

import neon.server.entity.Map;
import neon.systems.time.Calendar;

/**
 * The mutable server configuration.
 * 
 * @author mdriesen
 *
 */
public final class Configuration {
	public enum GameMode {
		TURN_BASED, REAL_TIME;
	}

	public static final int TICKS_PER_TURN = 5;
	public static final long PLAYER_UID = 0;
	
	private final Calendar calendar = new Calendar(0, TICKS_PER_TURN);

	private Map map;
	private GameMode mode = GameMode.TURN_BASED;
	private boolean running = false;
	
	/**
	 * Returns the game calendar.
	 * 
	 * @return
	 */
	public Calendar getCalendar() {
		return calendar;
	}
	
	/**
	 * Sets the current map.
	 * 
	 * @param map
	 */
	public void setCurrentMap(Map map) {
		this.map = Objects.requireNonNull(map, "map");
	}
	
	/**
	 * 
	 * @return	the current {@code Map}
	 */
	public Map getCurrentMap() {
		return map;
	}
	
	/**
	 * Sets the current game mode.
	 * 
	 * @param mode
	 */
	public void setMode(GameMode mode) {
		this.mode = Objects.requireNonNull(mode, "game mode");
	}
	
	/**
	 * 
	 * @return	the current {@code GameMode}
	 */
	public GameMode getMode() {
		return mode;
	}
	
	/**
	 * Checks whether a game is currently running.
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Sets whether a game is currently running or not.
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}
}
