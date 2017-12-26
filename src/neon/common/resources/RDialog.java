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

package neon.common.resources;

import java.util.ArrayList;
import java.util.Collection;

public class RDialog extends Resource {
	private final Topic root;
	
	public RDialog(String id, Topic root) {
		super(id, "dialog", "dialog");
		this.root = root;
	}
	
	public Topic getRoot() {
		return root;
	}
	
	public static class Topic {
		private final Collection<Topic> topics = new ArrayList<Topic>();
		private final String text;
		private final String id;
		
		public Topic(String id, String text) {
			this.id = id;
			this.text = text;
		}
		
		public String getID() {
			return id;
		}
		
		public Collection<Topic> getSubtopics() {
			return topics;
		}
		
		public String getText() {
			return text;
		}
		
		public void addTopic(Topic topic) {
			topics.add(topic);
		}
	}
}
