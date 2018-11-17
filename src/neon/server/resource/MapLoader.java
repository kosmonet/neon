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

package neon.server.resource;

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
import neon.server.entity.EntityManager;

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
	
	public MapLoader(NeonFileSystem files, EntityManager entities) {
		this.files = files;
	}
	
	@Override
	public RMap load(String id) throws IOException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		String name = root.getAttributeValue("name");
		int width = Integer.parseInt(root.getChild("size").getAttributeValue("width"));
		int height = Integer.parseInt(root.getChild("size").getAttributeValue("height"));
		String module = root.getAttributeValue("module");
		short uid = Short.parseShort(root.getAttributeValue("uid"));
		
		return new RMap(id, name, width, height, uid, module);		
	}
	
	@Override
	public void save(Resource resource) throws IOException {
		throw new UnsupportedOperationException("Server doesn't support saving maps yet.");
		
//		RMap map = RMap.class.cast(resource);
//		
//		Element root = new Element("map");
//		root.setAttribute("id", map.id);
//		root.setAttribute("name", map.name);
//		root.setAttribute("uid", Integer.toString(map.uid));
//		root.setAttribute("module", map.module);
//		
//		Element size = new Element("size");
//		size.setAttribute("width", Integer.toString(map.width));
//		size.setAttribute("height", Integer.toString(map.height));
//		root.addContent(size);
//		
//		Element entities = new Element("entities");
//		root.addContent(entities);
//		for (long uid : map.getEntities()) {
//			String type = "entity";
//			
//			if (tracker.getEntity(uid).hasComponent(CreatureInfo.class)) {
//				type = "creature";
//			}
//			
//			Element entity = new Element(type);
//			entity.setAttribute("uid", Long.toString(uid));
//			// we don't need to set the position, this is saved by the entity itself
//			entities.addContent(entity);
//		}
//		
//		Element terrain = new Element("terrain");
//		root.addContent(terrain);
//		Map<Rectangle, String> terrainLeaves = map.getTerrain().getElements();
//		for (Entry<Rectangle, String> entry : terrainLeaves.entrySet()) {
//			if(entry.getValue() != null) {
//				Element region = new Element("region");
//				region.setAttribute("x", Integer.toString(entry.getKey().x));
//				region.setAttribute("y", Integer.toString(entry.getKey().y));
//				region.setAttribute("w", Integer.toString(entry.getKey().width));
//				region.setAttribute("h", Integer.toString(entry.getKey().height));
//				region.setAttribute("id", entry.getValue());
//				terrain.addContent(region);
//			}
//		}
//		
//		Element elevation = new Element("elevation");
//		root.addContent(elevation);
//		Map<Rectangle, Integer> heightLeaves = map.getElevation().getElements();
//		for (Entry<Rectangle, Integer> entry : heightLeaves.entrySet()) {
//			if(entry.getValue() != null) {
//				Element region = new Element("region");
//				region.setAttribute("x", Integer.toString(entry.getKey().x));
//				region.setAttribute("y", Integer.toString(entry.getKey().y));
//				region.setAttribute("w", Integer.toString(entry.getKey().width));
//				region.setAttribute("h", Integer.toString(entry.getKey().height));
//				region.setAttribute("z", Integer.toString(entry.getValue()));
//				terrain.addContent(region);
//			}
//		}
//		
//		files.saveFile(new Document(root), translator, namespace, resource.id + ".xml");
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
