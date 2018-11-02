/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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

package neon.server.entity;

import neon.common.entity.Entity;
import neon.common.entity.components.Behavior;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Skills;
import neon.common.entity.components.Stats;
import neon.common.resources.RCreature;
import neon.systems.magic.Magic;

public final class CreatureBuilder implements EntityBuilder<RCreature> {
	@Override
	public Entity build(long uid, RCreature species) {
		Entity creature = new Entity(uid);
		creature.setComponent(new Shape(uid));
		creature.setComponent(new CreatureInfo(uid, species.id, species.name));
		creature.setComponent(new Graphics(uid, species.glyph, species.color));
		creature.setComponent(new Inventory(uid));
		creature.setComponent(new Behavior(uid));
		creature.setComponent(new Skills(uid));
		creature.setComponent(new Stats(uid, species));
		creature.setComponent(new Magic(uid));
		return creature;
	}
}
