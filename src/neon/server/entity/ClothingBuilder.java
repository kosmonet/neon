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
import neon.common.entity.components.Clothing;
import neon.common.resources.RItem;
import neon.systems.magic.Enchantment;

/**
 * An entity builder for clothing.
 * 
 * @author mdriesen
 *
 */
public final class ClothingBuilder implements EntityBuilder<RItem.Clothing> {
	private final ItemBuilder builder = new ItemBuilder();

	@Override
	public Entity build(long uid, RItem.Clothing resource) {
		Entity cloth = builder.build(uid, resource);
		cloth.setComponent(new Clothing(uid, resource.slot));
		if (resource.effect.isPresent()) {
			cloth.setComponent(new Enchantment(uid, resource.effect.get(), resource.magnitude));
		}
		return cloth;
	}
}
