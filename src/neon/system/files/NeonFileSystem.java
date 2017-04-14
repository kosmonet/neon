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

package neon.system.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Implements a virtual filesystem containing all assets related to a game. The
 * data from all loaded modules will be compacted into a single folder tree. 
 * Data from a parent module will be overwritten by its child modules (if 
 * present).
 * 
 * Support for temporary and save folders is included.
 * 
 * @author mdriesen
 *
 * TODO: wanneer een file opgevraagd wordt, gaat filesystem vanaf de laatste module kijken
 * of de gevraagde file aanwezig is. Is het misschien beter alle files in een hashmap 
 * bij te houden om die dan achteraf sneller op te kunnen vragen?
 */
public class NeonFileSystem {
	private final static Logger logger = Logger.getGlobal();
	
	private final ArrayList<String> modules = new ArrayList<>();
	private Path temporary, save;
	
	/**
	 * Creates an empty filesystem. Module folders should be added before the
	 * filesystem is usable.
	 */
	public NeonFileSystem() {
		// this constructor is here to prevent the variadic String constructor
		// being called with an empty array
	}
	
	/**
	 * Creates a new virtual filesystem using a list of module names. The 
	 * modules with the given names should be present in the data folder. The 
	 * module names should be given in the correct order (parent should appear 
	 * before any module that is dependent on it).
	 * 
	 * @param modules	the list of modules that have to be loaded
	 * @throws FileNotFoundException
	 */
	public NeonFileSystem(String... modules) throws FileNotFoundException {
		// first check if all modules are actually present and if so, store them in an arraylist
		for (String module : modules) {
			Path path = Paths.get("data", module);
			if (path.toFile().exists()) {
				this.modules.add(module);
				logger.info("path <" + module + "> found");
			} else {
				throw new FileNotFoundException("Module <" + module + "> is missing!");
			}
		}
		
		// reverse arraylist for file retrieval
		Collections.reverse(this.modules);
	}
	
	/**
	 * Adds a module folder to the virtual file system. Modules must be added 
	 * in the correct order (parent should appear before any module that is 
	 * dependent on it).
	 * 
	 * @param module
	 * @throws FileNotFoundException
	 */
	public void addModule(String module) throws FileNotFoundException {
		Path path = Paths.get("data", module);
		if (path.toFile().exists()) {
			modules.add(0, module);
			logger.info("path <" + module + "> found");
		} else {
			throw new FileNotFoundException("Module <" + module + "> is missing!");
		}		
	}
	
	/**
	 * Sets the path to the temporary folder.
	 * 
	 * @param path
	 * @throws IOException 
	 */
	public void setTemporaryFolder(Path path) throws IOException {
		// delete contents of existing temp folder
		if (Files.exists(path)) {
			delete(path.toFile());
		}

		// create new temp folder
		Files.createDirectories(path);
		temporary = path;			
		logger.config("temp folder set to " + path);
	}
	
	private void delete(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f);
			}
		}
		file.delete();
	}

	
	/**
	 * Sets the path to the folder of the current saved game.
	 * 
	 * @param path
	 * @throws NotDirectoryException
	 */
	public void setSaveFolder(Path path) throws NotDirectoryException {
		if (path.toFile().isDirectory()) {
			save = path;
			logger.config("save folder set to " + path);
		} else {
			throw new NotDirectoryException(path.toString() + " is not a folder");
		}
	}
	
	/**
	 * 
	 * @param path	the path of the requested file
	 * @return the requested file
	 * @throws FileNotFoundException
	 */
	public File loadFile(String... path) throws FileNotFoundException {
		// check the temp folder first
		if (temporary != null) {
			File file = Paths.get(temporary.toString(), path).toFile();
			if (file.exists()) {
				logger.finest("file " + Arrays.toString(path) + " found in temp");
				return file;
			}
		}
		
		// then check the save folder
		if (save != null) {
			File file = Paths.get(save.toString(), path).toFile();
			if (file.exists()) {
				logger.finest("file " + Arrays.toString(path) + " found in save");
				return file;
			}			
		}
		
		// copy path to larger array to make room for the module name
		String[] temp = new String[path.length + 1];
		System.arraycopy(path, 0, temp, 1, path.length);
		
		// check all loaded modules to see if the requested file is present in that module
		for (String module : modules) {
			temp[0] = module;
			File file = Paths.get("data", temp).toFile();
			if (file.exists()) {
				logger.finest("file " + Arrays.toString(path) + " found in module " + module);
				return file;
			}
		}
		
		// if we get here, file was not found, throw an exception
		throw new FileNotFoundException("File " + Arrays.toString(path) + " not found!");
	}
	
	/**
	 * Loads a file using the given translator.
	 * 
	 * @param translator
	 * @param path
	 * @return the translated file
	 * @throws IOException 
	 */
	public <T> T loadFile(Translator<T> translator, String... path) throws IOException {
		// we go through loadFile(String... path) to resolve the real path
		try (InputStream in = Files.newInputStream(loadFile(path).toPath())) {
			return translator.translate(in);
		}
	}
	
	/**
	 * Saves a file using the given translator. Files are saved by default to 
	 * the temporary folder. 
	 * 
	 * @param output
	 * @param translator
	 * @param path
	 * @throws IOException
	 */
	public <T> void saveFile(T output, Translator<T> translator, String... path) throws IOException {
		// first check if the parent folder already exists, and if not, create it
		Path parent = Paths.get(temporary.toString(), path).getParent();
		if (!Files.exists(parent)) {
			logger.fine("creating folder " + parent + " for file " + Arrays.toString(path));
			Files.createDirectories(parent);
		}

		try (OutputStream out = Files.newOutputStream(Paths.get(temporary.toString(), path))) {
			logger.finest("writing file " + Arrays.toString(path));
			translator.translate(output, out);
		}
	}
}
