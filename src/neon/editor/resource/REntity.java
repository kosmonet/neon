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

package neon.editor.resource;

import neon.entity.components.ShapeComponent;
import neon.entity.entities.Entity;

public abstract class REntity extends Entity {
	public final ShapeComponent shape;

	private final String id;
	
	REntity(int uid, String id) {
		super(uid);
		this.id = id;
		shape = new ShapeComponent(uid);
	}

	/**
	 * 
	 * @return	the id of the resource this entity is based on
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * 
	 * @return	the xml tag name this entity is saved with
	 */
	abstract String getType();
}
