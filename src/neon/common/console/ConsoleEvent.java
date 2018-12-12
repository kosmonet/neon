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

package neon.common.console;

import java.util.Objects;

import neon.common.event.NeonEvent;

/**
 * An event to signal that a message should be shown on the console.
 * 
 * @author mdriesen
 *
 */
public final class ConsoleEvent extends NeonEvent{
	private final String message;
	
	/**
	 * Initialize this event with a message. The message must not be null.
	 * 
	 * @param message	a {@code String} with the message.
	 */
	public ConsoleEvent(String message) {
		this.message = Objects.requireNonNull(message, "message");
	}
	
	/**
	 * Returns the message to be displayed.
	 * 
	 * @return	a {@code String} containing the message
	 */
	public String getMessage() {
		return message;
	}
}
