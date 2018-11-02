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
import neon.common.entity.components.Graphics;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Shape;
import neon.common.resources.RItem;

public class ItemBuilder implements EntityBuilder<RItem> {
	@Override
	public Entity build(long uid, RItem resource) {
		Entity item = new Entity(uid);
		item.setComponent(new Shape(uid));
		item.setComponent(new ItemInfo(uid, resource.id, resource.name, resource.price, resource.weight));
		item.setComponent(new Graphics(uid, resource.glyph, resource.color));
		return item;
	}
}
