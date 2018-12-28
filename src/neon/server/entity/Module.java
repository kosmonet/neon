/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

package neon.server.entity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import neon.common.files.FileUtils;
import neon.common.resources.RModule;

/**
 * A class to represent a module.
 * 
 * @author mdriesen
 *
 */
public final class Module {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final Set<String> maps;
	
	/**
	 * Initializes a new module.
	 * 
	 * @param module	the module resource
	 */
	public Module(RModule module) {
		Path path = Paths.get("data", module.id, "maps");
		maps = FileUtils.listFiles(path).stream().map(Files::getNameWithoutExtension).collect(ImmutableSet.toImmutableSet());
		LOGGER.info("module <" + module.id + "> contains " + maps.size() + " maps: " + maps);
	}

	/**
	 * Returns the names of the maps in a module.
	 * 
	 * @return	an unmodifiable {@code Set}
	 */
	public Set<String> getMaps() {
		return maps;
	}
}
