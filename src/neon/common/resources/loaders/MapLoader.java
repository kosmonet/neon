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

package neon.common.resources.loaders;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import com.google.common.io.Files;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RMap;
import neon.common.resources.Resource;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
public final class MapLoader implements ResourceLoader {
	private static final String NAMESPACE = "maps";
	private static final XMLTranslator TRANSLATOR = new XMLTranslator();
	
	private final NeonFileSystem files;
	
	/**
	 * Initializes a new map loader. The file system must not be null.
	 * 
	 * @param files	the file system used to load map resources
	 */
	public MapLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public RMap load(String id) throws IOException, DataConversionException {
		Element root = files.loadFile(TRANSLATOR, NAMESPACE, id + ".xml").getRootElement();
		String name = root.getAttributeValue("name");
		int width = root.getChild("size").getAttribute("width").getIntValue();
		int height = root.getChild("size").getAttribute("height").getIntValue();
		String module = root.getAttributeValue("module");
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		
		return new RMap(id, name, width, height, uid, module);		
	}
	
	@Override
	public void save(Resource resource) throws IOException {
		throw new UnsupportedOperationException("Server doesn't support saving maps yet.");
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(NAMESPACE).parallelStream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) throws IOException {
		files.deleteFile(NAMESPACE, id + ".xml");
	}
	
	@Override
	public String getNamespace() {
		return NAMESPACE;
	}
}
