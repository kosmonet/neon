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

package neon.client.resource;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Element;

import com.google.common.io.Files;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RMap;
import neon.common.resources.Resource;
import neon.common.resources.loaders.ResourceLoader;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
public final class MapLoader implements ResourceLoader {
	private static final String namespace = "maps";
	
	private final XMLTranslator translator = new XMLTranslator();
	private final NeonFileSystem files;
	
	public MapLoader(NeonFileSystem files) {
		this.files = files;
	}
	
	@Override
	public RMap load(String id) throws IOException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		
		RMap map = new RMap(id, name, width, height, uid);
		
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		
		return map;	
	}
	
	@Override
	public void save(Resource resource) {
		throw new UnsupportedOperationException("Client is not allowed to save resources.");
	}

	private void initTerrain(RMap map, Element terrain) {
		for (Element region : terrain.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			String id = region.getAttributeValue("id");
			map.getTerrain().insert(new Rectangle(x, y, width, height), id);
		}
	}
	
	private void initElevation(RMap map, Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int value = Integer.parseInt(region.getAttributeValue("v"));
			map.getElevation().insert(new Rectangle(x, y, width, height), value);
		}		
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(namespace).parallelStream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) {
		throw new UnsupportedOperationException("Client is not allowed to remove resources.");
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
}
