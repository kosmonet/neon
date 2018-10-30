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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

public class ConversationSystem {
	private final ResourceManager resources;
	private final EntityManager entities;
	private final EventBus bus;
	
	private RDialog currentDialog;
	private CreatureNode currentNode;
	
	public ConversationSystem(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.bus = bus;
		this.resources = resources;
		this.entities = entities;
	}
	
	/**
	 * Collects the initial list of topics when a new conversation with a 
	 * creature is started.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void onConversationStart(ConversationEvent.Start event) throws ResourceException {
		Entity listener = entities.getEntity(event.getSecond());
		ArrayList<Topic> topics = new ArrayList<>();
		currentDialog = resources.getResource("dialog", listener.getComponent(Dialog.class).getDialog());
		currentNode = currentDialog.getRoot();
		
		for (String id : currentNode.children) {
			PlayerNode topic = currentDialog.getPlayerNode(id);
			topics.add(new Topic(topic.id, topic.text));
		}
		
		bus.post(new ConversationEvent.Update(currentNode.text, topics));
	}

	@Subscribe
	private void onAnswer(ConversationEvent.Answer event) throws ResourceException {
		PlayerNode pnode = currentDialog.getPlayerNode(event.answer);

		if (pnode.type.equals(NodeType.END)) {
			bus.post(new ConversationEvent.End());
		} else {
			currentNode = currentDialog.getCreatureNode(pnode.children.get(0));
			ArrayList<Topic> topics = new ArrayList<>();
			for (String id : currentNode.children) {
				PlayerNode topic = currentDialog.getPlayerNode(id);
				switch (topic.type) {
				case CONTINUE:
					topics.add(new Topic(topic.id, "[continue]"));				
					break;
				case END:
					topics.add(new Topic(topic.id, topic.text + " [end]"));				
					break;
				case LINK:	// fallthrough
				case NONE:
					topics.add(new Topic(topic.id, topic.text));
					break;
				}
			}

			bus.post(new ConversationEvent.Update(currentNode.text, topics));
		}
	}
}
