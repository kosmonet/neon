/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

package neon.common.resources;

public class CGame extends Resource {
	private final String startMap;
	private final int x, y;
	
	private String currentMap;

	public CGame(String startMap, int startX, int startY) {
		super("game", "game", "config");
		this.startMap = startMap;
		currentMap = startMap;
		x = startX;
		y = startY;
	}
	
	/**
	 * 
	 * @return	the id of the starting map
	 */
	public String getStartMap() {
		return startMap;
	}
	
	public int getStartX() {
		return x;
	}
	
	public int getStartY() {
		return y;
	}
	
	/**
	 * Sets the id of the current map.
	 * 
	 * @param map
	 */
	public void setCurrentMap(String map) {
		currentMap = map;
	}
	
	/**
	 * 
	 * @return	the id of the current map
	 */
	public String getCurrentMap() {
		return currentMap;
	}
}
