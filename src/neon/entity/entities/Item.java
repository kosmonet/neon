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

package neon.entity.entities;

import neon.common.resources.RItem;
import neon.entity.components.Component;
import neon.entity.components.Graphics;
import neon.entity.components.Shape;

public class Item extends Entity {
	public Item(long uid, RItem item) {
		super(uid);
		components.put(Shape.class, new Shape(uid));
		components.put(Resource.class, new Resource(uid, item));
		components.put(Graphics.class, new Graphics(uid, item.glyph, item.color));		
	}
	
	@Override
	public String toString() {
		return components.getInstance(Resource.class).getResource().name;
	}
	
	public static class Resource implements Component {
		private final RItem resource;
		private final long uid;
		
		public Resource(long uid, RItem resource) {
			this.resource = resource;
			this.uid = uid;
		}
		
		@Override
		public long getEntity() {
			return uid;
		}
		
		public RItem getResource() {
			return resource;
		}
	}
}
