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
import java.util.HashMap;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

public class ConversationSystem {
	private final EntityManager entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public ConversationSystem(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.entities = entities;
		this.bus = bus;
		this.resources = resources;
	}
	
	/**
	 * Collects the initial list of topics when a new conversation with a 
	 * creature is started.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void talk(ConversationEvent.Start event) throws ResourceException {
		Entity speaker = entities.getEntity(event.getFirst());
		Entity listener = entities.getEntity(event.getSecond());
		
		ArrayList<Topic> topics = new ArrayList<>();
		String text = "";
		
		for (String id : resources.listResources("dialog")) {
			RDialog dialog = resources.getResource("dialog", id);
			text = dialog.getRoot().text;
			for (PNode topic : dialog.getRoot().children) {
				topics.add(new Topic(dialog.id, topic.id, topic.text, topic.child.id));
			}
		}
		
		bus.post(new ConversationEvent.Update(text, topics));
	}

	@Subscribe
	private void answer(ConversationEvent.Answer event) throws ResourceException {
		System.out.println("Answer: " + event.answer + "; dialog: " + event.resource);
		RDialog dialog = resources.getResource("dialog", event.resource);
		CNode child = dialog.getNode(event.child);
		ArrayList<Topic> topics = new ArrayList<>();
		
		for (PNode node : child.children) {
			topics.add(new Topic(event.resource, node.id, node.text, node.child.id));
		}

		bus.post(new ConversationEvent.Update(child.text, topics));
	}
}
