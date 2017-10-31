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

package neon.entity.entities;

import neon.common.resources.RCreature;
import neon.entity.components.BehaviorComponent;
import neon.entity.components.GraphicsComponent;
import neon.entity.components.InventoryComponent;
import neon.entity.components.ResourceComponent;
import neon.entity.components.ShapeComponent;
import neon.entity.components.SkillComponent;
import neon.entity.components.StatsComponent;

public class Creature extends Entity {
	public final InventoryComponent inventory;
	public final ShapeComponent shape;
	public final GraphicsComponent graphics;
	public final BehaviorComponent ai;
	public final StatsComponent stats;
	public final SkillComponent skills;
	public final BehaviorComponent brain;
	public final ResourceComponent<RCreature> info;
	
	public Creature(long uid, RCreature species) {
		super(uid);
		inventory = new InventoryComponent(uid);
		shape = new ShapeComponent(uid);
		graphics = new GraphicsComponent(uid, species.character, species.color);
		ai = new BehaviorComponent(uid);
		stats = new StatsComponent(uid, species);
		skills = new SkillComponent(uid);
		brain = new BehaviorComponent(uid);
		info = new ResourceComponent<>(uid, species);
	}
}
