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

package neon.editor.resource;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.google.common.io.Files;

import neon.editor.resource.RMap;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RCreature;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.common.resources.loaders.ResourceLoader;

/**
 * A loader for map resources.
 * 
 * @author mdriesen
 *
 */
public final class MapLoader implements ResourceLoader {
	private static final Logger logger = Logger.getGlobal();
	private static final String namespace = "maps";
	private static final XMLTranslator translator = new XMLTranslator();
	
	private final NeonFileSystem files;
	private final ResourceManager resources;
	
	public MapLoader(NeonFileSystem files, ResourceManager resources) {
		this.files = files;
		this.resources = resources;
	}
	
	@Override
	public RMap load(String id) throws IOException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		String module = root.getAttributeValue("module");
		RMap map = new RMap(id, name, width, height, uid, module);
		initTerrain(map, root.getChild("terrain"));
		initElevation(map, root.getChild("elevation"));
		initEntities(map, root.getChild("entities"));
		
		return map;
	}
	
	private void initTerrain(RMap map, Element terrain) {
		for (Element region : terrain.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			String id = region.getAttributeValue("id");
			
			map.getTerrain().insert(id, x, y, width, height);
		}
	}
	
	private void initElevation(RMap map, Element elevation) {
		for (Element region : elevation.getChildren("region")) {
			int width = Integer.parseInt(region.getAttributeValue("w"));
			int height = Integer.parseInt(region.getAttributeValue("h"));
			int x = Integer.parseInt(region.getAttributeValue("x"));
			int y = Integer.parseInt(region.getAttributeValue("y"));
			int value = Integer.parseInt(region.getAttributeValue("z"));
			
			map.getElevation().insert(value, x, y, width, height);
		}		
	}
	
	private void initEntities(RMap map, Element entities) {
		for (Element entity : entities.getChildren("creature")) {
			try {
				int uid = Integer.parseInt(entity.getAttributeValue("uid"));
				String id = entity.getAttributeValue("id");
				RCreature resource = resources.getResource("creatures", id);
				ICreature creature = new ICreature(uid, id, resource.glyph, resource.color);
				creature.shape.setX(Integer.parseInt(entity.getAttributeValue("x")));
				creature.shape.setY(Integer.parseInt(entity.getAttributeValue("y")));
				map.add(creature);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}		
	}

	@Override
	public void save(Resource resource) throws IOException {
		RMap map = RMap.class.cast(resource);
		Element root = new Element("map");
		root.setAttribute("id", map.id);
		root.setAttribute("name", map.name);
		root.setAttribute("uid", Short.toString(map.uid));
		root.setAttribute("module", map.module);
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(map.getWidth()));
		size.setAttribute("height", Integer.toString(map.getHeight()));
		root.addContent(size);
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		Map<Rectangle, String> leaves = map.getTerrain().getElements();
		for (Entry<Rectangle, String> entry : leaves.entrySet()) {
			if (entry.getValue() != null) {
				Element region = new Element("region");
				region.setAttribute("x", Integer.toString(entry.getKey().x));
				region.setAttribute("y", Integer.toString(entry.getKey().y));
				region.setAttribute("w", Integer.toString(entry.getKey().width));
				region.setAttribute("h", Integer.toString(entry.getKey().height));
				region.setAttribute("id", entry.getValue());
				terrain.addContent(region);
			}
		}
		
		Element elevation = new Element("elevation");
		root.addContent(elevation);
		
		Element entities = new Element("entities");
		for (REntity entity : map.getEntities()) {
			System.out.println(entity);
			Element element = new Element(entity.getType());
			element.setAttribute("uid", Long.toString(entity.uid));
			element.setAttribute("id", entity.getID());
			element.setAttribute("x", Integer.toString(entity.shape.getX()));
			element.setAttribute("y", Integer.toString(entity.shape.getY()));
			entities.addContent(element);
		}
		root.addContent(entities);
		
		files.saveFile(new Document(root), translator, namespace, resource.id + ".xml");
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(namespace).parallelStream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) throws IOException {
		files.deleteFile(namespace, id + ".xml");
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
}
