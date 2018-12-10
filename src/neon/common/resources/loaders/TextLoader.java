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

package neon.common.resources.loaders;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.io.Files;

import neon.common.files.NeonFileSystem;
import neon.common.files.StringTranslator;
import neon.common.resources.RText;
import neon.common.resources.Resource;

public class TextLoader implements ResourceLoader {
	private static final String namespace = "texts";

	private final StringTranslator translator = new StringTranslator();
	private final NeonFileSystem files;
	
	public TextLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public Resource load(String id) throws IOException {
		String text = files.loadFile(translator, namespace, id + ".html");
		return new RText(id, text);
	}

	@Override
	public void save(Resource resource) throws IOException {
		RText rt = RText.class.cast(resource);
		files.saveFile(rt.text, translator, namespace, resource.id + ".html");
	}

	@Override
	public Set<String> listResources() {
		return files.listFiles(namespace).stream()
				.map(Files::getNameWithoutExtension)
				.collect(Collectors.toSet());
	}

	@Override
	public void removeResource(String id) throws IOException {
		files.deleteFile(namespace, id + ".html");		
	}

	@Override
	public String getNamespace() {
		return namespace;
	}
}
