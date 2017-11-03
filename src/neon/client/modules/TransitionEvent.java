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

package neon.client.modules;

import neon.common.event.NeonEvent;
import neon.common.resources.RMap;

/**
 * An event to signal the transition between two client states.
 * 
 * @author mdriesen
 *
 */
public class TransitionEvent extends NeonEvent {
	private final String condition;
	private final RMap map;
	
	private boolean consumed = false;
	
	public TransitionEvent(String condition) {
		this.condition = condition;
		this.map = null;
	}
	
	/**
	 * Initializes a new transition event.
	 * 
	 * @param condition
	 * @param map		the current map the player is on
	 */
	public TransitionEvent(String condition, RMap map) {
		this.condition = condition;
		this.map = map;
	}
	
	/**
	 * @return	the current map
	 */
	public RMap getMap() {
		return map;
	}
	
	/**
	 * 
	 * @return the condition for this transition
	 */
	public String getCondition() {
		return condition;
	}
	
	/**
	 * Consumes this event, so it won't be handled by other bus subscribers.
	 */
	public void consume() {
		consumed = true;
	}
	
	/**
	 * 
	 * @return whether this event was consumed by another bus subscriber
	 */
	public boolean isConsumed() {
		return consumed;
	}
}
