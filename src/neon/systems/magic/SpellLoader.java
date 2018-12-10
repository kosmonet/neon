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

package neon.systems.magic;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.google.common.io.Files;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.Resource;
import neon.common.resources.loaders.ResourceLoader;

public final class SpellLoader implements ResourceLoader {
	private static final String namespace = "spells";
	private static final XMLTranslator translator = new XMLTranslator();
	
	private final NeonFileSystem files;
	
	public SpellLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public RSpell load(String id) throws IOException {
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		String name = root.getAttributeValue("name");
		Effect effect = Effect.valueOf(root.getAttributeValue("effect").toUpperCase());
		Target target = Target.valueOf(root.getAttributeValue("target").toUpperCase());
		int duration = Integer.parseInt(root.getAttributeValue("duration"));
		int magnitude = Integer.parseInt(root.getAttributeValue("magnitude"));
		
		RSpell spell = new RSpell(id, name, effect, target, duration, magnitude);
		return spell;
	}

	@Override
	public void save(Resource resource) throws IOException {
		Element spell = new Element("spell");
		files.saveFile(new Document(spell), translator, namespace, resource.id + ".xml");
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
