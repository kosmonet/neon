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

package neon.systems.conversation;

import java.io.IOException;
import java.util.ArrayList;
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

final class DialogLoader implements ResourceLoader {
	private static final String namespace = "dialog";
	private static final XMLTranslator translator = new XMLTranslator();
	
	private final NeonFileSystem files;
	
	public DialogLoader(NeonFileSystem files) {
		this.files = Objects.requireNonNull(files, "file system");
	}
	
	@Override
	public RDialog load(String id) throws IOException {
		RDialog dialog = new RDialog(id);
		Element root = files.loadFile(translator, namespace, id + ".xml").getRootElement();
		loadCreatureNode(root, dialog);	// recursively load all nodes, starting from root
		return dialog;
	}

	private PlayerNode loadPlayerNode(Element node, RDialog dialog) {
		NodeType type = NodeType.NONE;
		if (node.getAttribute("type") != null) {
			type = NodeType.valueOf(node.getAttributeValue("type").toUpperCase());
		} 
		
		ArrayList<String> nodes = new ArrayList<>();
		if (type == NodeType.LINK) {
			nodes.add(node.getAttributeValue("link"));
		} else {
			for (Element child : node.getChildren("cnode")) {
				nodes.add(loadCreatureNode(child, dialog).id);
			}			
		}
		
		PlayerNode pnode = new PlayerNode(node.getAttributeValue("id"), node.getChildText("text"), nodes, type);
		dialog.addNode(pnode);
		return pnode;
	}
	
	private CreatureNode loadCreatureNode(Element node, RDialog dialog) {
		ArrayList<String> nodes = new ArrayList<>();
		for (Element child : node.getChildren("pnode")) {
			nodes.add(loadPlayerNode(child, dialog).id);
		}
		
		CreatureNode cnode = new CreatureNode(node.getAttributeValue("id"), node.getChildText("text"), nodes);
		dialog.addNode(cnode);
		return cnode;
	}
	
	@Override
	public void save(Resource resource) throws IOException {
		Element dialog = new Element("dialog");
		files.saveFile(new Document(dialog), translator, namespace, resource.id + ".xml");
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
