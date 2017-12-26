/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

import org.jdom2.Element;

import neon.common.resources.RDialog;
import neon.common.resources.RDialog.Topic;

public class DialogLoader implements ResourceLoader<RDialog> {
	@Override
	public RDialog load(Element root) {
		Element cnode = root.getChild("cnode");
		
		Topic topic = new Topic(cnode.getAttributeValue("id"), cnode.getChildText("text"));
		for (Element pnode : cnode.getChildren("pnode")) {
			Topic sub = new Topic(pnode.getAttributeValue("id"), pnode.getChildText("text"));
			topic.addTopic(sub);
		}
		
		RDialog dialog = new RDialog(root.getAttributeValue("id"), topic);
		return dialog;
	}

	@Override
	public Element save(RDialog resource) {
		Element dialog = new Element("dialog");
		
		return dialog;
	}
}
