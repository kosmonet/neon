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

package neon.common.resources;

import java.util.Objects;

/**
 * @author mdriesen
 */
public final class RMap extends Resource {
	/** The uid of this map. */
	public final short uid;
	/** The fancy display name. */
	public final String name;
	/** The width of the map. */
	public final int width;
	/** The height of the map. */
	public final int height;
	/** The module this map belongs to. */
	public final String module;
	
	/**
	 * Initializes this map without terrain, elevation or entities. The name 
	 * and module id must not be null.
	 * 
	 * @param id
	 * @param name
	 * @param width
	 * @param height
	 * @param uid
	 * @param module
	 */
	public RMap(String id, String name, int width, int height, short uid, String module) {
		super(id, "maps");
		this.name = Objects.requireNonNull(name, "name");
		this.module = Objects.requireNonNull(module, "module id");
		this.width = width;
		this.height = height;
		this.uid = uid;
	}
}
