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

package neon.common.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;

/**
 * A collection of utility methods to work with the file system.
 * 
 * @author mdriesen
 *
 */
public class FileUtils {
	private final static Logger logger = Logger.getGlobal();

	/**
	 * Move the contents of a folder to another folder. This method will 
	 * overwrite existing items in the destination folder.
	 * 
	 * @param from
	 * @param to
	 */
	public static void copyFolder(Path from, Path to) {
		logger.info("copying files from " + from + " to " + to);
		
		TreeTraverser<File> traverser = Files.fileTreeTraverser();
		for (File file : traverser.preOrderTraversal(from.toFile())) {
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
	 * Clears all content from the given folder.
	 * 
	 * @param folder
	 */
	public static void clearFolder(Path folder) {
		// delete folder
		if (folder.toFile().exists()) {
			delete(folder.toFile());
		}
		
		// recreate folder
		folder.toFile().mkdir();
	}
	
	/**
	 * Lists all the files in the given folder. If the given folder does not
	 * exist or is not actually a folder, an empty array is returned.
	 * 
	 * @param path
	 * @return
	 */
	public static String[] listFiles(Path folder) {
		File file = folder.toFile();
		if (file.exists() && file.isDirectory()) {
			return file.list();
		} else {
			return new String[0];
		}
	}
	
	private static void delete(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f);
			}
		}
		file.delete();
	}
}
