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

import com.google.common.collect.ImmutableSet;

/**
 * Configuration information for the client. This resource should be created 
 * by the server when a new game is started and sent to the client, it cannot 
 * initially be loaded by the resource manager.
 * 
 * @author mdriesen
 *
 */
public final class CClient extends Resource {
	public final String title;
	public final String subtitle;
	public final String intro;
	
	private final Set<String> species;
	
	/**
	 * Initializes the client configuration resource. Duplicates in the given
	 * iterable of species are ignored. Title, subtitle and intro text must not
	 * be null.
	 * 
	 * @param title
	 * @param subtitle
	 * @param species
	 * @param intro
	 */
	public CClient(String title, String subtitle, Iterable<String> species, String intro) {
		super("client", "config");
		this.title = Objects.requireNonNull(title, "title");
		this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
		this.intro = Objects.requireNonNull(intro, "intro");
		this.species = ImmutableSet.copyOf(species);
	}

	/**
	 * Returns a set of all species the player can choose from during 
	 * character creation.
	 * 
	 * @return an unmodifiable {@code Set} with creature id's
	 */
	public Set<String> getPlayableSpecies() {
		return species;
	}
}
