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
import java.util.List;

import com.google.common.collect.ImmutableList;

import neon.common.event.NeonEvent;

public abstract class ConversationEvent extends NeonEvent {	
	/**
	 * Event to start a conversation.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Start extends ConversationEvent {
		final long speaker;
		final long listener;
		
		public Start(long speaker, long listener) {
			this.speaker = speaker;
			this.listener = listener;
		}
	}
	
	/**
	 * Event to send an updated list of topics to the client.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Update extends ConversationEvent {
		public final String answer;
		public final long listener;

		private final List<Topic> topics;
		
		Update(String answer, ArrayList<Topic> topics, long listener) {
			this.answer = answer;
			this.topics = ImmutableList.copyOf(topics);
			this.listener = listener;
		}
		
		public List<Topic> getTopics() {
			return topics;
		}
	}
	
	/**
	 * Event to send the player's chosen answer to the server.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Answer extends ConversationEvent {
		final String answer;
		
		public Answer(String answer) {
			this.answer = answer;
		}
	}
	
	public static final class End extends ConversationEvent {}
}
