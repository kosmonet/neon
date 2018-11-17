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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;

/**
 * Configuration information for the server. This resource should be created 
 * by the server when a new game is started, it cannot initially be loaded 
 * by the resource manager.
 * 
 * @author mdriesen
 *
 */
public final class CServer extends Resource {
	private static final Logger logger = Logger.getGlobal();
	
	private final Set<String> modules;
	private final Level level;

	/**
	 * Initializes this server configuration resource with the given set of
	 * modules. A {@code LinkedHashSet} is used to prevent duplicates and to 
	 * preserve the correct load order of the modules. 
	 * 
	 * @param modules
	 * @param logLevel
	 */
	public CServer(LinkedHashSet<String> modules, String logLevel) {
		super("server", "config");
		this.modules = ImmutableSet.copyOf(modules);
		level = Level.parse(logLevel);
		logger.config("module load order: " + modules);
	}
	
	/**
	 * Returns a set of modules that preserves the correct load 
	 * order, as defined in the neon.ini configuration file.
	 * 
	 * @return 	an ordered, unmodifiable {@code Set} of module id's
	 */
	public Set<String> getModules() {
		return modules;
	}
	
	/**
	 * 
	 * @return 	the required logging {@code Level}
	 */
	public Level getLogLevel() {
		return level;
	}
	
	/**
	 * 
	 * @param module
	 * @return	whether the given module was loaded
	 */
	public boolean hasModule(String module) {
		return modules.contains(module);
	}
}
