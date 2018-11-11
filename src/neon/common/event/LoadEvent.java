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

package neon.common.event;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * This event is used to signal game loading-related information.
 * 
 * @author mdriesen
 *
 */
public abstract class LoadEvent extends NeonEvent {
	/**
	 * This event is used to request a list of saved games from the server.
	 * 
	 * @author mdriesen
	 */
	public static final class Load extends LoadEvent {}

	/**
	 * This event requests the server to start a loaded game.
	 * 
	 * @author mdriesen
	 */
	public static final class Start extends LoadEvent {
		public final String save;
		
		public Start(String save) {
			this.save = save;
		}
	}
	
	/**
	 * This events lists the available saved games.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class List extends LoadEvent {
		private final Set<String> saves;

		/**
		 * Initializes this event. Duplicates in the collection of saves are
		 * ignored.
		 * 
		 * @param saves
		 */
		public List(Collection<String> saves) {
			this.saves = ImmutableSet.copyOf(saves);
		}

		/**
		 * Returns an unmodifiable set of all saved games.
		 * 
		 * @return
		 */
		public Set<String> getSaves() {
			return saves;
		}
	}
}
