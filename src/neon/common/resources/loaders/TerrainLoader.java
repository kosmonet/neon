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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.google.common.io.Files;

import javafx.scene.paint.Color;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RTerrain;
import neon.common.resources.RTerrain.Modifier;
import neon.common.resources.Resource;
import neon.util.GraphicsUtils;

/**
 * This resource loader takes care of loading/saving terrain resources 
 * from/to disk. 
 * 
 * @author mdriesen
 *
 */
public final class TerrainLoader implements ResourceLoader {
	private static final String namespace = "terrain";
	
	private final XMLTranslator translator = new XMLTranslator();
	private final NeonFileSystem files;
	
	public TerrainLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public RTerrain load(String id) throws IOException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		char glyph = root.getChild("graphics").getAttributeValue("text").charAt(0);
		Color color = Color.web(root.getChild("graphics").getAttributeValue("color"));
		String name = root.getAttributeValue("name");
		
		HashSet<Modifier> modifiers = new HashSet<>();		
		for (Element modifier : root.getChildren("modifier")) {
			modifiers.add(Modifier.valueOf(modifier.getText().toUpperCase()));
		}
		
		return new RTerrain(id, name, glyph, color, modifiers);
	}

	@Override
	public void save(Resource resource) throws IOException {
		RTerrain terrain = RTerrain.class.cast(resource);
		
		Element root = new Element("terrain");
		root.setAttribute("id", terrain.id);
		root.setAttribute("name", terrain.name);
		
		Element graphics = new Element("graphics");
		graphics.setAttribute("text", Character.toString(terrain.glyph));
		graphics.setAttribute("color", GraphicsUtils.getColorString(terrain.color));
		root.addContent(graphics);
		
		for (Modifier modifier : terrain.getModifiers()) {
			Element mod = new Element("modifier");
			mod.setText(modifier.toString());
			root.addContent(mod);
		}
		
		files.saveFile(new Document(root), translator, namespace, resource.id + ".xml");
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(namespace).stream()
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
