/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CGame extends Resource {
	public final String title;
	
	private final Set<String> species = new HashSet<>();
	private final String startMap;
	private final int x, y;

	public CGame(String title, Collection<String> species, String startMap, int startX, int startY) {
		super("game", "config", "config");
		this.species.addAll(species);
		this.title = title;
		this.startMap = startMap;
		x = startX;
		y = startY;
	}
	
	/**
	 * Returns the id's of all playable species.
	 * 
	 * @return a {@code Set<String>} with id's
	 */
	public Set<String> getPlayableSpecies() {
		return species;
	}
	
	public String getStartMap() {
		return startMap;
	}
	
	public int getStartX() {
		return x;
	}
	
	public int getStartY() {
		return y;
	}
}
