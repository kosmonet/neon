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

/**
 * A state in the client finite state machine. 
 * 
 * @author mdriesen
 *
 */
public abstract class State {
	private boolean active;
	
	/**
	 * Performs the entry actions of this state.
	 * 
	 * @param event	the event that caused this state to be entered
	 */
	public abstract void enter(TransitionEvent event);
	
	/**
	 * Performs the exit actions of this state.
	 * 
	 * @param event	the event that caused this state to be exited
	 */
	public abstract void exit(TransitionEvent event);
	
	/**
	 * Returns whether this state is the currently active state of the state
	 * machine.
	 * 
	 * @return whether this state is active or not
	 */
	boolean isActive() {
		return active;
	}
	
	/**
	 * Sets the active status of this state.
	 * 
	 * @param active	the new status
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}
