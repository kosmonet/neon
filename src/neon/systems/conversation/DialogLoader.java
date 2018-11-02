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

import java.util.ArrayList;

import org.jdom2.Element;

import neon.common.resources.loaders.ResourceLoader;

final class DialogLoader implements ResourceLoader<RDialog> {
	@Override
	public RDialog load(Element root) {
		RDialog dialog = new RDialog(root.getAttributeValue("id"));
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
	public Element save(RDialog resource) {
		Element dialog = new Element("dialog");
		
		return dialog;
	}
}
