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
import org.jdom2.Document;
import org.jdom2.Element;

import com.google.common.io.Files;

import javafx.scene.paint.Color;
import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RCreature;
import neon.common.resources.Resource;
import neon.util.GraphicsUtils;

/**
 * A resource loader specifically for creatures.
 * 
 * @author mdriesen
 *
 */
public final class CreatureLoader implements ResourceLoader {
	private static final String namespace = "creatures";
	private static final XMLTranslator translator = new XMLTranslator();
	
	private final NeonFileSystem files;
	
	public CreatureLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public RCreature load(String id) throws IOException, DataConversionException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		RCreature.Builder builder = new RCreature.Builder(id);
		builder.setName(root.getAttributeValue("name"));

		char glyph = root.getChild("graphics").getAttributeValue("char").charAt(0);
		Color color = Color.web(root.getChild("graphics").getAttributeValue("color"));
		builder.setGraphics(glyph, color);

//		String description = root.getChildText("description").replaceAll("\t", "");
		String description = root.getChildTextNormalize("description");
		builder.setDescription(description);
		
		Element stats = root.getChild("stats");
		int str = stats.getAttribute("str").getIntValue();
		int con = stats.getAttribute("con").getIntValue();
		int dex = stats.getAttribute("dex").getIntValue();
		int іnt = stats.getAttribute("int").getIntValue();
		int wis = stats.getAttribute("wis").getIntValue();
		int cha = stats.getAttribute("cha").getIntValue();
		builder.setStats(str, con, dex, іnt, wis, cha);
		builder.setSpeed(root.getAttribute("speed").getIntValue());

		return builder.build();
	}
	
	@Override
	public void save(Resource resource) throws IOException {
		RCreature rc = RCreature.class.cast(resource);
		
		Element creature = new Element("creature");
		creature.setAttribute("id", rc.id);
		creature.setAttribute("name", rc.name);
		creature.setAttribute("speed", Integer.toString(rc.speed));
		
		Element graphics = new Element("graphics");
		graphics.setAttribute("char", Character.toString(rc.glyph));
		graphics.setAttribute("color", GraphicsUtils.getColorString(rc.color));
		creature.addContent(graphics);
		
		Element stats = new Element("stats");
		stats.setAttribute("str", Integer.toString(rc.strength));
		stats.setAttribute("con", Integer.toString(rc.constitution));
		stats.setAttribute("dex", Integer.toString(rc.dexterity));
		stats.setAttribute("int", Integer.toString(rc.intelligence));
		stats.setAttribute("wis", Integer.toString(rc.wisdom));
		stats.setAttribute("cha", Integer.toString(rc.charisma));
		creature.addContent(stats);
		
		files.saveFile(new Document(creature), translator, namespace, resource.id + ".xml");
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
