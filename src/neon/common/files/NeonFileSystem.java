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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

/**
 * A virtual file system, containing all assets related to a game. The
 * data from all loaded modules will be compacted into a single folder tree. 
 * Data from a parent module will be overwritten by its child modules (if 
 * present).
 * 
 * Support for temporary and save folders is included.
 * 
 * @author mdriesen
 *
 */
public final class NeonFileSystem {
	private static final Logger LOGGER = Logger.getGlobal();
	
	private final List<String> modules = new ArrayList<>();
	private final boolean writable;
	private Path temporary, save;
	
	/**
	 * Creates an empty, writable file system. Module folders should be added 
	 * before the file system is usable.
	 */
	public NeonFileSystem() {
		this(Permissions.WRITABLE);
	}
	
	/**
	 * Creates an empty file system. Module folders should be added 
	 * before the file system is usable.
	 * 
	 * @param permission	the write {@code Permission} of the file system
	 */
	public NeonFileSystem(Permissions permission) {
		this.writable = Permissions.WRITABLE.equals(permission);
	}
	
	/**
	 * Adds a module folder to the virtual file system. Modules must be added 
	 * in the correct order (parent module should appear before any module that 
	 * is dependent on it). A module must only be added once.
	 * 
	 * @param module	the id of a module
	 * @throws FileNotFoundException	if the module is missing
	 */
	public void addModule(String module) throws FileNotFoundException {
		if (modules.contains(module)) {
			throw new IllegalStateException("Module '" + module + "' already present");
		}
		
		Path path = Paths.get("data", module);
		if (path.toFile().exists()) {
			modules.add(0, module);
			LOGGER.info("module '" + module + "' found");
		} else {
			throw new FileNotFoundException("Module '" + module + "' is missing");
		}		
	}
	
	/**
	 * Sets the path to the temporary folder. If the file system is writable,
	 * the folder will also be emptied.
	 * 
	 * @param path	the {@code Path} to the temporary folder
	 * @throws IOException	if the temporary folder can't be set
	 */
	public void setTemporaryFolder(Path path) throws IOException {
		// delete contents of existing temp folder
		if (writable) {
			FileUtils.clearFolder(path);
		}

		// create new temp folder if it didn't exist
		Files.createDirectories(path);
		temporary = path;			
		LOGGER.config("temp folder path set to [" + path + "]");
	}
	
	/**
	 * Sets the path to the folder of the current saved game.
	 * 
	 * @param path	the {@code Path} to the saved game folder
	 * @throws NotDirectoryException	if the given path doesn't point to a folder
	 */
	public void setSaveFolder(Path path) throws NotDirectoryException {
		if (path.toFile().isDirectory()) {
			save = path;
			LOGGER.config("save folder set to [" + path + "]");
		} else {
			throw new NotDirectoryException("[" + path + "] is not a folder");
		}
	}
	
	/**
	 * Returns a file. The temp folder is searched first. If the file is not
	 * present, the save folder is searched. If the file is not present, all 
	 * modules are searched in reverse order. If the file is still not found,
	 * an exception is thrown.
	 * 
	 * @param path	a {@code String[]} representation of a file path
	 * @return the requested file
	 * @throws FileNotFoundException	if the file was not found
	 */
	public File loadFile(String... path) throws FileNotFoundException {
		// check the temp folder first
		if (temporary != null) {
			File file = Paths.get(temporary.toString(), path).toFile();
			if (file.exists()) {
				LOGGER.finest("file " + Arrays.toString(path) + " found in temp");
				return file;
			}
		}
		
		// then check the save folder
		if (save != null) {
			File file = Paths.get(save.toString(), path).toFile();
			if (file.exists()) {
				LOGGER.finest("file " + Arrays.toString(path) + " found in save");
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
				LOGGER.finest("file " + Arrays.toString(path) + " found in module '" + module + "'");
				return file;
			}
		}
		
		// if we get here, file was not found, throw an exception
		throw new FileNotFoundException("File " + Arrays.toString(path) + " not found");
	}
	
	/**
	 * Loads and translates a file at the given path.
	 * 
	 * @param translator	the {@code Translator} to use
	 * @param path	a {@code String[]} representation of a file path
	 * @return the translated file
	 * @throws IOException	if a file at the given path can't be loaded
	 */
	public <T> T loadFile(Translator<T> translator, String... path) throws IOException {
		// we go through loadFile(String... path) to resolve the real path
		try (InputStream in = Files.newInputStream(loadFile(path).toPath())) {
			return translator.translate(in);
		}
	}
	
	/**
	 * Translates and saves an object to a file at the given path. Files are 
	 * saved by default to the temporary folder. 
	 * 
	 * @param output	the object to save
	 * @param translator	the {@code Translator} to use
	 * @param path	a {@code String[]} representation of a file path
	 * @throws IOException	if the file can't be saved
	 */
	public <T> void saveFile(T output, Translator<T> translator, String... path) throws IOException {
		// check if the filesystem is writable
		if (!writable) {
			throw new IOException("Filesystem is not writable");			
		}
		
		// check if the temp folder exists
		if (temporary == null) {
			throw new IOException("No temp folder registered");
		}
		
		// check if the parent folder already exists, and if not, create it
		Path parent = Paths.get(temporary.toString(), path).getParent();
		if (parent != null && !Files.exists(parent)) {
			LOGGER.fine("creating folder [" + parent + "] for file " + Arrays.toString(path));
			Files.createDirectories(parent);
		}

		try (OutputStream out = Files.newOutputStream(Paths.get(temporary.toString(), path))) {
			LOGGER.finest("writing file " + Arrays.toString(path));
			translator.translate(output, out);
		}
	}
	
	/**
	 * Lists all the files in the given folder. If a file was not found for 
	 * any reason (including possible {@code IOException}s), it will not be
	 * listed.
	 * 
	 * @param folder	a {@code String[]} representation of a folder path
	 * @return	an immutable {@code Set<String>} of filenames
	 */
	public Set<String> listFiles(String... folder) {
		Set<Path> files = new HashSet<>();
		
		// check the temp folder first
		if (temporary != null) {
			Path path = Paths.get(temporary.toString(), folder);
			if (Files.isDirectory(path)) {
				try (Stream<Path> paths = Files.list(path)) {
					paths.map(Path::getFileName).forEach(files::add);					
				} catch (IOException e) {
					LOGGER.warning("could not list files in folder " + path);
				}
			}
		}

		// then check the save folder
		if (save != null) {
			Path path = Paths.get(save.toString(), folder);
			if (Files.isDirectory(path)) {
				try (Stream<Path> paths = Files.list(path)) {
					paths.map(Path::getFileName).forEach(files::add);					
				} catch (IOException e) {
					LOGGER.warning("could not list files in the save folder " + path);
				}
			}
		}
		
		// copy path to larger array to make room for the module name
		String[] mod = new String[folder.length + 1];
		System.arraycopy(folder, 0, mod, 1, folder.length);
		
		// check all loaded modules
		for (String module : modules) {
			mod[0] = module;
			Path path = Paths.get("data", mod);
			if (Files.isDirectory(path)) {
				try (Stream<Path> paths = Files.list(path)) {
					paths.map(Path::getFileName).forEach(files::add);					
				} catch (IOException e) {
					LOGGER.warning("could not list files in the temp folder " + path);
				}
			}
		}
		
		return files.stream().map(Path::toString).collect(ImmutableSet.toImmutableSet());
	}
	
	/**
	 * Deletes the file with the given path. This will only delete files in the
	 * temp folder.
	 * 
	 * @param path	a {@code String[]} representation of a file path
	 * @throws IOException	if the file can't be deleted
	 */
	public void deleteFile(String... path) throws IOException {
		// check if the temp folder exists
		if (temporary == null) {
			throw new IOException("No temp folder registered");
		}

		// check if the file system is writable
		if (!writable) {
			throw new IOException("Filesystem is not writable");
		}

		File file = Paths.get(temporary.toString(), path).toFile();
		if (file.exists()) {
			file.delete();
			LOGGER.finest("deleted file " + Arrays.toString(path));
		}
	}
}
