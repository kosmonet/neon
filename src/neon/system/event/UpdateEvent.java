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

package neon.system.event;

import java.util.ArrayList;
import java.util.Collection;

import neon.system.resources.Resource;

/**
 * An event containing resource updates for the client.
 * 
 * @author mdriesen
 *
 */
public class UpdateEvent extends ClientEvent {
	protected final Collection<Resource> resources = new ArrayList<>();
	
	public Collection<Resource> getResources() {
		return resources;
	}
	
	public static class Start extends UpdateEvent {
		public Start(Collection<Resource> resources) {
			this.resources.addAll(resources);
		}
	}
	
	public static class Map extends UpdateEvent {
		
	}
	
	public static class Resources extends UpdateEvent {
		
	}
}
