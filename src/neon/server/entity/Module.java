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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import neon.common.files.NeonFileSystem;
import neon.common.resources.RModule;

public final class Module {
	private static final Logger logger = Logger.getGlobal();
	
	private final Set<String> maps;
	
	public Module(RModule module, NeonFileSystem files) {
		Path path = Paths.get("data", module.id, "maps");
		Set<String> set = new HashSet<>();
		
		try {
			if (Files.exists(path)) {
				set = Files.list(path).map(map -> map.getFileName().toString().replaceAll(".xml", "")).collect(Collectors.toSet());
			}
		} catch (IOException e) {
			logger.warning("error loading maps in module <" + module.id + ">");
		}
		
		maps = ImmutableSet.copyOf(set);
		logger.info("module <" + module.id + "> contains " + maps.size() + " maps: " + maps);
	}
	
	public Set<String> getMaps() {
		return maps;
	}
}
