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

package neon.client.states;

import com.google.common.eventbus.Subscribe;

/**
 * A transition between two states in the state machine.
 * 
 * @author mdriesen
 *
 */
public class Transition {
	private final String condition;
	private final State from, to;
	
	/**
	 * Creates a transition between the from and to {@code State}s, under the given
	 * condition.
	 * 
	 * @param from
	 * @param to
	 * @param condition
	 */
	public Transition(State from, State to, String condition) {
		this.condition = condition;
		this.from = from;
		this.to = to;
	}
	
	/**
	 * Transitions between two states.
	 * 
	 * @param event
	 */
	@Subscribe
	public void transition(TransitionEvent event) {
		if (from.isActive() && !event.isConsumed() && event.getCondition().equals(condition)) {
			event.consume();
			from.setActive(false);
			from.exit(event);
			to.setActive(true);
			to.enter(event);
		}
	}
}