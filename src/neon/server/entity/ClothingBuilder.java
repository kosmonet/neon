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

import neon.common.resources.RItem;
import neon.entity.components.Clothing;
import neon.entity.entities.Entity;
import neon.entity.entities.Item;

public class ClothingBuilder implements EntityBuilder<RItem.Clothing> {
	@Override
	public Entity build(long uid, RItem.Clothing resource) {
		Item cloth = new Item(uid, resource);
		cloth.setComponent(new Clothing(uid, resource.slot));
		return cloth;
	}
}
