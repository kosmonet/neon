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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CGame extends Resource {
	private final String startMap;
	private final int x, y, money;
	private final List<String> items = new ArrayList<>();
	private final Set<String> spells = new HashSet<>();
	
	private String currentMap;
	private GameMode mode = GameMode.TURN_BASED;
	
	public CGame(String startMap, int startX, int startY, int startMoney) {
		super("game", "config");
		this.startMap = startMap;
		currentMap = startMap;
		x = startX;
		y = startY;
		money = startMoney;
	}
	
	public void addStartItems(Collection<String> items) {
		this.items.addAll(items);
	}
	
	public void addStartSpells(Collection<String> spells) {
		this.spells.addAll(spells);
	}
	
	public List<String> getStartItems() {
		return items;
	}
	
	public Set<String> getStartSpells() {
		return spells;
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
	
	public int getStartMoney() {
		return money;
	}
	
	/**
	 * 
	 * @return	the id of the current map
	 */
	public String getCurrentMap() {
		return currentMap;
	}
	
	public void setMode(GameMode mode) {
		this.mode = mode;
	}
	
	public GameMode getMode() {
		return mode;
	}
}
