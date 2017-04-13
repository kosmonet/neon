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

package neon.system.resources;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A mod resource. This resource is not intended to be handled by the resource 
 * manager, it is capable of saving and loading itself.
 * 
 * @author mdriesen
 *
 */
public class RModule extends Resource {
	private final Set<String> species = new HashSet<>();
	private final String title;

	public RModule(String id, String title) {
		super(id, "module");
		this.title = title;
	}
	
	/**
	 * 
	 * @return the title of the current game
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Add species to the list of playable species.
	 * 
	 * @param species
	 */
	public void addPlayableSpecies(Collection<String> species) {
		this.species.addAll(species);
	}
	
	/**
	 * 
	 * @return a set of playable species
	 */
	public Set<String> getPlayableSpecies() {
		return species;
	}
}
