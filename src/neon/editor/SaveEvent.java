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

package neon.editor;

import neon.system.event.NeonEvent;
import neon.system.resources.Resource;

/**
 * An event to signal that an edited resource should be saved.
 * 
 * @author mdriesen
 *
 */
public class SaveEvent extends NeonEvent {
	private final Resource resource;
	
	public SaveEvent(String message, Resource resource) {
		super("module");
		this.resource = resource;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Resource> T getResource() {
		return (T) resource;
	}
}
