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

package neon.common.event;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import neon.common.resources.CClient;
import neon.common.resources.CServer;

/**
 * An event containing all configuration information for the client.
 * 
 * @author mdriesen
 *
 */
public final class ConfigurationEvent extends NeonEvent {
	private final Set<String> modules;
	
	public final String title;
	public final String subtitle;
	
	/**
	 * Initializes this event with game configuration data.
	 * 
	 * @param config
	 */
	public ConfigurationEvent(CClient cc, CServer cs) {
		modules = ImmutableSet.copyOf(cs.getModules());
		title = cc.title;
		subtitle = cc.subtitle;
	}
	
	/**
	 * Returns an unmodifiable {@code Set} that preserves the correct load 
	 * order of modules, as defined in the neon.ini configuration file.
	 * 
	 * @return	the modules loaded in this game
	 */
	public Set<String> getModules() {
		return modules;
	}
}
