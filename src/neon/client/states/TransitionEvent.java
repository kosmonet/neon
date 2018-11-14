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

package neon.client.states;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import neon.common.event.ClientEvent;

/**
 * An event to signal the transition between two client states.
 * 
 * @author mdriesen
 *
 */
public final class TransitionEvent extends ClientEvent {
	private final String condition;
	private final ClassToInstanceMap<Object> parameters = MutableClassToInstanceMap.create();
	
	private boolean consumed = false;
	
	/**
	 * Initializes a new transition event with parameters.
	 * 
	 * @param condition
	 * @param parameters
	 */
	public TransitionEvent(String condition, Object... parameters) {
		this.condition = condition;
		for (Object parameter : parameters) {
			this.parameters.put(parameter.getClass(), parameter);			
		}
	}
	
	/**
	 * Returns a parameter of the given type.
	 * 
	 * @param type
	 * @return
	 */
	<T extends Object> T getParameter(Class<T> type) {
		return parameters.getInstance(type);
	}
	
	/**
	 * 
	 * @return the condition for this transition
	 */
	String getCondition() {
		return condition;
	}
	
	/**
	 * Consumes this event, so it won't be handled by other bus subscribers.
	 */
	void consume() {
		consumed = true;
	}
	
	/**
	 * 
	 * @return whether this event was consumed by another bus subscriber
	 */
	boolean isConsumed() {
		return consumed;
	}
}
