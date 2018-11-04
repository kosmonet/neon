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
import java.util.Collections;
import java.util.List;

import neon.common.event.NeonEvent;

public abstract class ConversationEvent extends NeonEvent {	
	/**
	 * Event to start a conversation.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Start extends ConversationEvent {
		private final long one;
		private final long two;
		
		public Start(long one, long two) {
			this.one = one;
			this.two = two;
		}
		
		long getFirst() {
			return one;
		}
		
		long getSecond() {
			return two;
		}		
	}
	
	/**
	 * Event to send an updated list of topics to the client.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Update extends ConversationEvent {
		private final String answer;
		private final ArrayList<Topic> topics;
		
		Update(String answer, ArrayList<Topic> topics) {
			this.answer = answer;
			this.topics = new ArrayList<Topic>(topics);
		}
		
		public String getAnswer() {
			return answer;
		}
		
		public List<Topic> getTopics() {
			return Collections.unmodifiableList(topics);
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
