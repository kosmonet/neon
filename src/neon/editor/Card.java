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

import neon.system.resources.Resource;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

/**
 * An index card to keep track of a resource id with some extra information 
 * for rendering in e.g. {@code TreeView}s.
 * 
 * @author mdriesen
 *
 */
public class Card {
	private final String id;
	private final ResourceManager resources;
	private final String namespace;
	private boolean changed = false;
	
	/**
	 * Initializes this index card. The status of the referred resource is 
	 * initially considered to be unchanged.
	 * 
	 * @param namespace
	 * @param id
	 * @param resources
	 */
	public Card(String namespace, String id, ResourceManager resources) {
		this.namespace = namespace;
		this.id = id;
		this.resources = resources;
	}
	
	public String toString() {
		return id;
	}
	
	/**
	 * Sets the changed status of this card.
	 * 
	 * @param changed
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	/**
	 * 
	 * @return whether the referred resource has been changed
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * 
	 * @return the resource referred to by this index card
	 * @throws ResourceException
	 */
	public <T extends Resource> T getResource() throws ResourceException {
		return resources.getResource(namespace, id);
	}
}
