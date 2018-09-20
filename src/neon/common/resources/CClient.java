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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration information for the client. This resource should be created 
 * by the server when a new game is started and sent to the client, it cannot 
 * initially be loaded by the resource manager.
 * 
 * @author mdriesen
 *
 */
public class CClient extends Resource {
	public final String title;
	public final String subtitle;
	public final String intro;
	
	private final Set<String> species = new HashSet<>();
	
	/**
	 * Initializes the client configuration resource.
	 */
	public CClient(String title, String subtitle, Collection<String> species, String intro) {
		super("client", "config");
		this.title = title;
		this.subtitle = subtitle;
		this.species.addAll(species);
		this.intro = intro;
	}

	/**
	 * Returns the id's of all playable species.
	 * 
	 * @return a {@code Set<String>} with id's
	 */
	public Set<String> getPlayableSpecies() {
		return species;
	}
}
