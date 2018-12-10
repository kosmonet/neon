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

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class CGame extends Resource {
	/** The id of the map the game starts on. */
	public final String map;
	/** The starting x position. */
	public final int startX;
	/** The starting y position. */
	public final int startY;
	/** The amount of money the player starts with. */
	public final int startMoney;

	private final Iterable<String> items;
	private final Set<String> spells;
	
	public CGame(String startMap, int startX, int startY, int startMoney, Iterable<String> items, Iterable<String> spells) {
		super("game", "config");
		this.map = Objects.requireNonNull(startMap, "start map");
		this.startX = startX;
		this.startY = startY;
		this.startMoney = startMoney;
		
		this.items = ImmutableList.copyOf(items);
		this.spells = ImmutableSet.copyOf(spells);
	}
	
	/**
	 * Returns the items the player start the game with.
	 * 
	 * @return	an unmodifiable {@code Iterable} of item id's
	 */
	public Iterable<String> getStartItems() {
		return items;
	}
	
	/**
	 * A set of spells the player starts the game with.
	 * 
	 * @return	an unmodifiable {@code Set} of spell id's
	 */
	public Set<String> getStartSpells() {
		return spells;
	}
}
