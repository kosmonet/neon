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

package neon.system.event;

import neon.system.resources.CGame;

/**
 * An event containing all configuration information for the client.
 * 
 * @author mdriesen
 *
 */
public class ClientConfigurationEvent extends ClientEvent {
	private final String[] species;
	private final String title;
	
	/**
	 * Initializes this event with game configuration data.
	 * 
	 * @param cg
	 */
	public ClientConfigurationEvent(CGame cg) {
		species = new String[cg.getPlayableSpecies().size()];
		cg.getPlayableSpecies().toArray(species);
		title = cg.getTitle();
	}
	
	/**
	 * 
	 * @return the title of the current game
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * 
	 * @return the playable species in this game
	 */
	public String[] getPlayableSpecies() {
		return species;
	}
}
