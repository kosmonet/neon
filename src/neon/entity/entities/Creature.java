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

import neon.common.resources.RCreature;
import neon.entity.components.Behavior;
import neon.entity.components.Component;
import neon.entity.components.Graphics;
import neon.entity.components.Inventory;
import neon.entity.components.Shape;
import neon.entity.components.Skills;
import neon.entity.components.Stats;

public class Creature extends Entity {
	public Creature(long uid, RCreature species) {
		super(uid);
		components.put(Shape.class, new Shape(uid));
		components.put(Resource.class, new Resource(uid, species));
		components.put(Graphics.class, new Graphics(uid, species.glyph, species.color));
		components.put(Inventory.class, new Inventory(uid));
		components.put(Behavior.class, new Behavior(uid));
		components.put(Skills.class, new Skills(uid));
		components.put(Stats.class, new Stats(uid, species));
	}

	public static class Resource implements Component {
		private final RCreature resource;
		private final long uid;
		
		public Resource(long uid, RCreature resource) {
			this.resource = resource;
			this.uid = uid;
		}
		
		@Override
		public long getEntity() {
			return uid;
		}
		
		public RCreature getResource() {
			return resource;
		}
	}
}
