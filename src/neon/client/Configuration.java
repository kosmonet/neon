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

/**
 * Configuration for the client.
 * 
 * @author mdriesen
 *
 */
public class Configuration {
	public static final long PLAYER_UID = 0;
	
	private boolean paused = true;
	private Map map;
	
	/**
	 * Sets the map to be displayed.
	 * 
	 * @param map
	 */
	public void setCurrentMap(Map map) {
		this.map = map;
	}
	
	/**
	 * Gets the map to be displayed.
	 * 
	 * @return
	 */
	public Map getCurrentMap() {
		return map;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public boolean isPaused() {
		return paused;
	}
}
