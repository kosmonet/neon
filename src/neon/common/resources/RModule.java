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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A module resource.
 * 
 * @author mdriesen
 *
 */
public class RModule extends Resource {
	/**
	 * The title that should be displayed on the start screen. 
	 */
	public final String title;
	public final String subtitle;
	public final String intro;
	public final String startMap;

	private final Set<String> species = new HashSet<>();
	private final Set<String> parents = new HashSet<>();
	private final List<String> items = new ArrayList<>();
	private final Set<String> spells = new HashSet<>();
	private final int x, y, money;

	public RModule(String id, String title, String subtitle, String startMap, int startX, int startY, String intro, int startMoney) {
		super(id, "global");
		this.title = title;
		this.subtitle = subtitle;
		this.intro = intro;
		this.startMap = startMap;
		x = startX;
		y = startY;
		money = startMoney;
	}
	
	public void addStartItem(String id) {
		items.add(id);
	}
	
	public void addStartSpell(String id) {
		spells.add(id);
	}
	
	public Collection<String> getStartItems() {
		return Collections.unmodifiableCollection(items);
	}
	
	public Set<String> getStartSpells() {
		return Collections.unmodifiableSet(spells);
	}
	
	/**
	 * Adds a creature to the list of playable species.
	 * 
	 * @param creature the id of a playable creature
	 */
	public void addPlayableSpecies(String creature) {
		species.add(creature);
	}
	
	/**
	 * Adds creatures to the list of playable species.
	 * 
	 * @param creatures
	 */
	public void addPlayableSpecies(Collection<String> creatures) {
		species.addAll(creatures);
	}
	
	/**
	 * 
	 * @return a set of playable species
	 */
	public Set<String> getPlayableSpecies() {
		return Collections.unmodifiableSet(species);
	}
	
	/**
	 * Adds modules to the list of parent modules.
	 * 
	 * @param species
	 */
	public void addParent(String parent) {
		parents.add(parent);
	}
	
	/**
	 * 
	 * @return a set of parent modules
	 */
	public Set<String> getParents() {
		return Collections.unmodifiableSet(parents);
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
}
