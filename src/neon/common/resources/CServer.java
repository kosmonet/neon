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

import java.util.LinkedHashSet;

import com.google.common.collect.HashBiMap;

/**
 * Configuration information for the server. This resource should be created 
 * by the server when a new game is started, it cannot initially be loaded 
 * by the resource manager.
 * 
 * @author mdriesen
 *
 */
public class CServer extends Resource {
	private final LinkedHashSet<String> modules = new LinkedHashSet<>();
	private final String level;
	private final HashBiMap<String, Short> uids = HashBiMap.create();

	/**
	 * Initializes this server configuration resource with the given set of
	 * modules. A {@source LinkedHashSet} is used to prevent doubles and to 
	 * preserve the correct load order of the modules. 
	 * 
	 * @param modules
	 * @param level
	 */
	public CServer(LinkedHashSet<String> modules, String level) {
		super("server", "config", "config");
		this.modules.addAll(modules);
		this.level = level;
		
		short index = 1;
		for(String module : modules) {
			uids.put(module, index++);
		}
	}
	
	/**
	 * @return the list of modules to be loaded
	 */
	public String[] getModules() {
		String[] list = new String[modules.size()];
		return modules.toArray(list);
	}
	
	/**
	 * 
	 * @return the required logging {@code Level}
	 */
	public String getLogLevel() {
		return level;
	}
	
	/**
	 * 
	 * @param module
	 * @return	the uid of the module
	 */
	public short getModuleUID(String module) {
		return uids.get(module);
	}
}
