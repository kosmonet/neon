/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The game configuration resource.
 * 
 * @author mdriesen
 *
 */
public final class CGame extends Resource {
	/** The id of the map the game starts on. */
	public final String map;
	/** The starting x position. */
	public final int startX;
	/** The starting y position. */
	public final int startY;
	/** The amount of money the player starts with. */
	public final int startMoney;
	/** The starting time. */
	public final int time;

	private final List<String> items;
	private final Set<String> spells;
	
	/**
	 * Initializes a new game resource. The start map must not be null.
	 * 
	 * @param startMap
	 * @param startX
	 * @param startY
	 * @param startMoney
	 * @param time
	 * @param items
	 * @param spells
	 */
	public CGame(String startMap, int startX, int startY, int startMoney, int time, Iterable<String> items, Iterable<String> spells) {
		super("game", "config");
		this.map = Objects.requireNonNull(startMap, "start map");
		this.startX = startX;
		this.startY = startY;
		this.startMoney = startMoney;
		this.time = time;
		
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(items, map, spells, startMoney, startX, startY, time);
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!super.equals(other)) {
			return false;
		} else if (other instanceof CGame) {
			CGame cg = (CGame) other;
			return Objects.equals(items, cg.items) && Objects.equals(map, cg.map) 
					&& Objects.equals(spells, cg.spells) && startMoney == cg.startMoney 
					&& startX == cg.startX && startY == cg.startY && time == cg.time;
		} else {
			return false;
		} 
	}
}
