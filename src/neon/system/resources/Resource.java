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

package neon.system.resources;

/**
 * A class that represents a resource that is loaded from disk. Every resource
 * has at least an id and a resource type.
 * 
 * @author mdriesen
 *
 */
public abstract class Resource {
	private String id, type;
	
	/**
	 * Creates a new resource with the given id and type.
	 * 
	 * @param id
	 * @param type
	 */
	public Resource(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	/**
	 * @return the resource id
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Sets the ID of this resource.
	 * 
	 * @param id
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * @return the resource type
	 */
	public String getType() {
		return type;
	}
}
