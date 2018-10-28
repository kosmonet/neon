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

public class DialogLoader implements ResourceLoader<RDialog> {
	@Override
	public RDialog load(Element root) {
		CNode node = loadCNode(root.getChild("cnode"));
		RDialog dialog = new RDialog(root.getAttributeValue("id"), node);
		return dialog;
	}

	private PNode loadPNode(Element node) {	
		CNode child = loadCNode(node.getChild("cnode"));
		return new PNode(node.getAttributeValue("id"), node.getChildText("text"), child);
	}
	
	private CNode loadCNode(Element node) {
		ArrayList<PNode> nodes = new ArrayList<>();
		for (Element child : node.getChildren("pnode")) {
			nodes.add(loadPNode(child));
		}
		
		return new CNode(node.getAttributeValue("id"), node.getChildText("text"), nodes);
	}
	
	@Override
	public Element save(RDialog resource) {
		Element dialog = new Element("dialog");
		
		return dialog;
	}
}
