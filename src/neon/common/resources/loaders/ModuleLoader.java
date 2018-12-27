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

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.RModule;
import neon.common.resources.Resource;

/**
 * Responsible for loading and saving module resources.
 * 
 * @author mdriesen
 *
 */
public final class ModuleLoader implements ResourceLoader {
	private final XMLTranslator translator = new XMLTranslator();
	private final NeonFileSystem files;
	
	public ModuleLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public RModule load(String id) throws IOException, DataConversionException {
		Element root = files.loadFile(translator, id + ".xml").getRootElement();
		String title = root.getChildText("title");
		String subtitle = root.getChildText("subtitle");
		String map = root.getChild("start").getAttributeValue("map");
		String intro = root.getChildren("intro").isEmpty() ? "" : root.getChildText("intro");
		Element start = root.getChild("start");
		int x = start.getAttribute("x").getIntValue();
		int y = start.getAttribute("y").getIntValue();		
		int money = start.getAttribute("money").getIntValue();
		
		RModule.Builder builder = new RModule.Builder(id).setTitle(title).setSubtitle(subtitle).setIntro(intro);
		builder.setStartMap(map).setStartMoney(money).setStartPosition(x, y);
		
		for (Element species : root.getChild("playable").getChildren()) {
			builder.addPlayableSpecies(species.getText());
		}
		
		for (Element parent : root.getChild("parents").getChildren()) {
			builder.addParentModule(parent.getText());
		}
		
		for (Element item : start.getChildren("item")) {
			builder.addStartItem(item.getAttributeValue("id"));
		}
		
		for (Element item : start.getChildren("spell")) {
			builder.addStartSpell(item.getAttributeValue("id"));
		}
		
		return builder.build();
	}

	@Override
	public void save(Resource resource) throws IOException {
		RModule module = RModule.class.cast(resource);
		
		Element root = new Element("module");
		root.setAttribute("id", module.id);
		Element title = new Element("title");
		title.setText(module.title);
		root.addContent(title);
		
		Element start = new Element("start");
		start.setAttribute("map", module.map);
		start.setAttribute("x", Integer.toString(module.x));
		start.setAttribute("y", Integer.toString(module.y));
		root.addContent(start);
		
		if (!module.intro.isEmpty()) {
			Element intro = new Element("intro");
			intro.setText(module.intro);
			root.addContent("intro");
		}
		
		Element playable = new Element("playable");
		root.addContent(playable);
		for (String species : module.getPlayableSpecies()) {
			Element id = new Element("id");
			id.setText(species);
			playable.addContent(id);
		}
		
		Element parents = new Element("parents");
		root.addContent(parents);
		for (String parent : module.getParentModules()) {
			Element id = new Element("id");
			id.setText(parent);
			parents.addContent(id);
		}
		
		files.saveFile(new Document(root), translator, resource.id + ".xml");
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles().parallelStream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) throws IOException {
		throw new UnsupportedOperationException("Module removal not supported.");
	}
	
	@Override
	public String getNamespace() {
		return "global";
	}
}
