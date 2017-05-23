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

package neon.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import neon.entity.entities.Entity;

public class EntityManager implements EntityProvider {
	private final Map<Long, Entity> entities = new HashMap<>();

	public Entity getEntity(long uid) {
		return entities.get(uid);
	}
	
	public void addEntity(Entity entity) {
		entities.put(entity.uid, entity);
	}
	
	public Collection<Entity> getEntities() {
		return entities.values();
	}
}
