/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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
import java.util.Objects;
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
	
	private final LinkedHashSet<String> modules;
	private final Level level;

	/**
	 * Initializes this server configuration resource with the given set of
	 * modules. A ordered set is used to prevent duplicates and to 
	 * preserve the correct load order of the modules. 
	 * 
	 * @param modules	a {@code LinkedHashSet<String>} of module id's
	 * @param logLevel	the granularity of the logging
	 */
	public CServer(LinkedHashSet<String> modules, String logLevel) {
		super("server", "config");
		this.modules = new LinkedHashSet<String>(modules);
		level = Level.parse(logLevel);
		logger.config("module load order: " + modules);
	}
	
	/**
	 * Returns a set of modules that preserves the correct load 
	 * order, as defined in the neon.ini configuration file.
	 * 
	 * @return 	an ordered, immutable {@code Set} of module id's
	 */
	public Set<String> getModules() {
		return ImmutableSet.copyOf(modules);
	}
	
	/**
	 * Removes a module from the load order.
	 * 
	 * @param module	the module id
	 */
	public void removeModule(String module) {
		modules.remove(module);
	}
	
	/**
	 * Returns the logging granularity.
	 * 
	 * @return 	the required logging {@code Level}
	 */
	public Level getLogLevel() {
		return level;
	}
	
	/**
	 * Checks whether a module was in the load order.
	 * 
	 * @param module	a module id
	 * @return	whether the given module was loaded
	 */
	public boolean hasModule(String module) {
		return modules.contains(module);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime*result + Objects.hash(level, modules);
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!super.equals(other)) {
			return false;
		} else if (other instanceof CServer) {
			CServer cs = (CServer) other;
			return Objects.equals(level, cs.level) && Objects.equals(modules, cs.modules);
		} else {
			return false;
		} 
	}
}
