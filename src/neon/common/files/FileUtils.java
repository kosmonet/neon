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

package neon.common.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Traverser;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

/**
 * A collection of utility methods to work with the file system.
 * 
 * @author mdriesen
 *
 */
public final class FileUtils {
	private static final Logger logger = Logger.getGlobal();
	
	// suppress default constructor for noninstantiability
	private FileUtils() {
		throw new AssertionError();
	}

	/**
	 * Copy the contents of a source folder to a destination folder. This 
	 * method will overwrite existing items in the destination folder.
	 * 
	 * @param from	the {@code Path} to the source folder
	 * @param to	the {@code Path} to the destination folder
	 */
	public static void copyFolder(Path from, Path to) {
		logger.info("copying files from " + from + " to " + to);
		
		Traverser<File> traverser = Files.fileTraverser();
		for (File file : traverser.depthFirstPreOrder(from.toFile())) {
			// parent folder should not be copied!
			if (!file.isDirectory()) {
				// construct the destination path from the origin path
				Path origin = Paths.get(file.getPath());
				Path relative = from.relativize(origin);
				Path destination = to.resolve(relative);
				logger.finest("copying file " + relative + " to " + to);

				try {
					Files.copy(file, destination.toFile());
				} catch (IOException e) {
					logger.severe("could not write file " + destination);
				}
			} else if (!file.equals(from.toFile())) {
				Path origin = Paths.get(file.getPath());
				Path relative = from.relativize(origin);
				Path destination = to.resolve(relative);

				if (!destination.toFile().exists()) {
					destination.toFile().mkdirs();
				}
			}
		}
	}
	
	/**
	 * Moves a source folder to a new destination. If the destination folder 
	 * already exists, the contents will be overwritten.
	 * 
	 * @param from	the {@code Path} to the source folder
	 * @param to	the {@code Path} to the destination folder
	 */
	public static void moveFolder(Path from, Path to) {
		logger.info("moving files from " + from + " to " + to);
		
		Traverser<File> traverser = Files.fileTraverser();
		
		// first move all the files
		for (File file : traverser.depthFirstPreOrder(from.toFile())) {
			// parent folder should not be moved!
			if (!file.isDirectory()) {
				// construct the destination path from the origin path
				Path origin = Paths.get(file.getPath());
				Path relative = from.relativize(origin);
				Path destination = to.resolve(relative);

				try {
					destination.toFile().delete();
					Files.move(file, destination.toFile());
				} catch (IOException e) {
					logger.severe("could not write file " + destination);
				}
			} else if (!file.equals(from.toFile())) {
				Path origin = Paths.get(file.getPath());
				Path relative = from.relativize(origin);
				Path destination = to.resolve(relative);

				if (!destination.toFile().exists()) {
					destination.toFile().mkdirs();
				}
			}
		}
		
		// then clean up all the leftovers
		clearFolder(from);
	}
	
	/**
	 * Clears all content from a folder.
	 * 
	 * @param folder	the {@code Path} to the folder to be cleared
	 */
	public static void clearFolder(Path folder) {
		logger.info("clearing folder " + folder);

		try {
			MoreFiles.deleteDirectoryContents(folder, RecursiveDeleteOption.ALLOW_INSECURE);
		} catch (IOException e) {
			throw new IllegalArgumentException("can't clear folder " + folder, e);
		}
	}
	
	/**
	 * Lists all the files in the given folder. If the given folder does not
	 * exist or is not actually a folder, an empty set is returned.
	 * 
	 * @param folder	the {@code Path} to a folder
	 * @return	an unmodifiable {@code Set<String>} of file names
	 */
	public static Set<String> listFiles(Path folder) {
		File file = folder.toFile();
		if (file.exists() && file.isDirectory()) {
			return ImmutableSet.copyOf(file.list());
		} else {
			return Collections.emptySet();
		}
	}
}
