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

package neon.common.event;

import java.util.Arrays;

import neon.common.resources.CClient;
import neon.common.resources.CServer;

/**
 * An event containing all configuration information for the client.
 * 
 * @author mdriesen
 *
 */
public class ClientConfigurationEvent extends NeonEvent {
	private final String[] species;
	private final String[] modules;
	private final String title;
	private final String subtitle;
	
	/**
	 * Initializes this event with game configuration data.
	 * 
	 * @param config
	 */
	public ClientConfigurationEvent(CClient cc, CServer cs) {
		species = cc.getPlayableSpecies().toArray(new String[cc.getPlayableSpecies().size()]);
		title = cc.title;
		subtitle = cc.subtitle;
		modules = cs.getModules();
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
	 * @return the subtitle of the current game
	 */
	public String getSubtitle() {
		return subtitle;
	}
	
	/**
	 * 
	 * @return the playable species in this game
	 */
	public String[] getPlayableSpecies() {
		return Arrays.copyOf(species, species.length);
	}
	
	/**
	 * 
	 * @return	the modules loaded in this game
	 */
	public String[] getModules() {
		return Arrays.copyOf(modules, modules.length);
	}
}
