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

package neon.server.systems;

import java.util.HashMap;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.event.ConversationEvent;
import neon.common.resources.RDialog;
import neon.common.resources.RDialog.Topic;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.entities.Creature;

public class ConversationSystem implements NeonSystem {
	private final EntityProvider entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public ConversationSystem(ResourceManager resources, EntityProvider entities, EventBus bus) {
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
		Creature speaker = entities.getEntity(event.getFirst());
		Creature listener = entities.getEntity(event.getSecond());
		System.out.println(speaker + " is talking to " + listener);
		
		RDialog dialog = resources.getResource("dialog", "test1");
		HashMap<String, String> topics = new HashMap<>();
		for (Topic topic : dialog.getRoot().getSubtopics()) {
			topics.put(topic.getID(), topic.getText());
		}
		
		bus.post(new ConversationEvent.Update(dialog.getRoot().getText(), topics));
	}

	@Subscribe
	private void answer(ConversationEvent.Answer event) {
		System.out.println("Answer: " + event.getAnswer());
	}
}
