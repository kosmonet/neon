/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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

package neon.common.resources;

import java.util.Objects;

/**
 * A class that represents a resource that is loaded from disk. Every resource
 * has at least an id and a resource type.
 * 
 * @author mdriesen
 *
 */
public abstract class Resource {
	/** The resource id. */
	public final String id;
	/** The namespace this resource belongs to. */
	public final String namespace;
	
	/**
	 * Creates a new resource with the given id and type. Type and id must not
	 * be null.
	 * 
	 * @param id	a {@code String}
	 * @param type	a {@code String}
	 */
	protected Resource(String id, String namespace) {
		this.id = Objects.requireNonNull(id, "id");
		this.namespace = Objects.requireNonNull(namespace, "namespace");
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + namespace + ":" + id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, namespace);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Resource) {
			Resource r = (Resource) other;
			return Objects.equals(id, r.id) && Objects.equals(namespace, r.namespace);
		} else { 
			return false;
		}
	}
}
