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

package neon.common.event;

import java.util.HashMap;

public abstract class ConversationEvent extends NeonEvent {
	/**
	 * Event to start a conversation.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Start extends ConversationEvent {
		private final long one;
		private final long two;
		
		public Start(long one, long two) {
			this.one = one;
			this.two = two;
		}
		
		public long getFirst() {
			return one;
		}
		
		public long getSecond() {
			return two;
		}		
	}
	
	/**
	 * Event to send an updated list of topics to the client.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Update extends ConversationEvent {
		private final String answer;
		private final HashMap<String, String> topics = new HashMap<>();
		
		public Update(String answer, HashMap<String, String> topics) {
			this.answer = answer;
			this.topics.putAll(topics);
		}
		
		public String getAnswer() {
			return answer;
		}
		
		public HashMap<String, String> getTopics() {
			return topics;
		}
	}
	
	/**
	 * Event to send the player's chosen answer to the server.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Answer extends ConversationEvent {
		private final String answer;
		
		public Answer(String answer) {
			this.answer = answer;
		}
		
		public String getAnswer() {
			return answer;
		}
	}
}
