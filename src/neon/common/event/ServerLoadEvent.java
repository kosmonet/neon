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

/**
 * This event is used to signal game loading-related information.
 * 
 * @author mdriesen
 *
 */
public class ServerLoadEvent extends ServerEvent {
	/**
	 * This event is used to request of list of saved games from the server.
	 * 
	 * @author mdriesen
	 */
	public static class List extends ServerLoadEvent {}

	/**
	 * This event requests the server to start a loaded game.
	 * 
	 * @author mdriesen
	 */
	public static class Start extends ServerLoadEvent {
		public final String save;
		
		public Start(String save) {
			this.save = save;
		}
	}
}
