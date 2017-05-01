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

import neon.system.resources.RMap;
import neon.system.resources.Resource;

/**
 * An event containing a map and all associated resources for the client. This
 * event is typically received when the player moves to another map.
 * 
 * @author mdriesen
 *
 */
public class UpdateEvent extends ClientEvent {
	private final Collection<Resource> resources = new ArrayList<Resource>();
	private final RMap map;
	
	public UpdateEvent(RMap map) {
		this.map = map;
	}
	
	public Collection<Resource> getResources() {
		return resources;
	}
	
	public RMap getMap() {
		return map;
	}
}
