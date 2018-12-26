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

package neon.editor;

import java.util.Objects;

import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

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
	private final boolean original;
	private final int hash;

	private boolean changed = false;
	private boolean redefined = false;
	
	/**
	 * Initializes this index card. The status of the referred resource is 
	 * initially considered to be unchanged.
	 * 
	 * @param namespace
	 * @param id
	 * @param resources
	 * @param original
	 */
	public Card(String namespace, String id, ResourceManager resources, boolean original) {
		this.namespace = namespace;
		this.id = id;
		this.resources = resources;
		this.original = original;
		hash = Objects.hash(namespace, id);
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!Card.class.isInstance(object)) {
			return false;
		} else {
			Card card = (Card) object;
			return namespace.equals(card.namespace) && id.equals(card.id);				
		}
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
	
	public void setRedefined(boolean redefined) {
		this.redefined = redefined;
	}
	
	public boolean isRedefined() {
		return redefined;
	}
	
	/**
	 * 
	 * @return whether the referred resource was already defined by a parent mod
	 */
	public boolean isOriginal() {
		return original;
	}
	
	/**
	 * 
	 * @return the resource referred to by this index card
	 * @throws ResourceException	if the resource can't be loaded
	 */
	public <T extends Resource> T getResource() throws ResourceException {
		return resources.getResource(namespace, id);
	}
	
	/**
	 * An extra type of card to allow the resource tree views to be divided 
	 * into subcategories of different types of items, creatures, ...
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Type extends Card {
		public Type(String type) {
			super(null, type, null, false);
		}
		
		@Override
		public boolean equals(Object object) {
			return this == object;
		}
	}
}
