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

package neon.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Element;

import com.google.common.io.Files;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.CClient;
import neon.common.resources.Resource;
import neon.common.resources.loaders.ResourceLoader;

/**
 * A loader for the client configuration.
 * 
 * @author mdriesen
 *
 */
public final class ConfigurationLoader implements ResourceLoader {
	private static final String NAMESPACE = "config";
	private static final XMLTranslator TRANSLATOR = new XMLTranslator();
	
	private final NeonFileSystem files;
	
	/**
	 * The file system must not be null.
	 * 
	 * @param files
	 */
	public ConfigurationLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}

	@Override
	public Resource load(String id) throws IOException {
		Element root = files.loadFile(TRANSLATOR, NAMESPACE, id + ".xml").getRootElement();
		String title = root.getAttributeValue("title");
		String subtitle = root.getAttributeValue("subtitle");
		String intro = root.getChildText("intro");
		
		Set<String> playable = new HashSet<>();
		for (Element species : root.getChild("playable").getChildren()) {
			playable.add(species.getAttributeValue("id"));
		}
		
		return new CClient(title, subtitle, playable, intro);
	}

	@Override
	public void save(Resource resource) {
		throw new UnsupportedOperationException("Client is not allowed to save resources.");
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(NAMESPACE).stream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) {
		throw new UnsupportedOperationException("Client is not allowed to remove resources.");		
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}
}
