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

import neon.common.resources.RMap;

/**
 * An event that is sent to the server.
 * 
 * @author mdriesen
 *
 */
public class ServerEvent extends NeonEvent { 
	public static class Start extends ServerEvent {
		private final RMap map;
		
		public Start(RMap map) {
			this.map = map;
		}
		
		public RMap getMap() {
			return map;
		}
	}
	
	public static class Inventory extends ServerEvent {}
}
