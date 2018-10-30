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

package neon.systems.conversation;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import neon.common.event.NeonEvent;

public abstract class ConversationEvent extends NeonEvent {
	private final static Gson gson = new Gson();
	
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
		private final String topics;
		
		public Update(String answer, ArrayList<Topic> topics) {
			this.answer = answer;
			this.topics = gson.toJson(topics);
		}
		
		public String getAnswer() {
			return answer;
		}
		
		public ArrayList<Topic> getTopics() {
			Type type = new TypeToken<ArrayList<Topic>>(){}.getType();
			return gson.fromJson(topics, type);
		}
	}
	
	/**
	 * Event to send the player's chosen answer to the server.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Answer extends ConversationEvent {
		final String answer;
		
		public Answer(String answer) {
			this.answer = answer;
		}
	}
	
	public static class End extends ConversationEvent {}
}
