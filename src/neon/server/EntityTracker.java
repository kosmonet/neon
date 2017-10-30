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

package neon.server;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import neon.entity.EntityProvider;
import neon.entity.entities.Entity;

/**
 * This class tracks all loaded entities in the current game (and by extension
 * all loaded modules and maps). 
 * 
 * @author mdriesen
 *
 */
public class EntityTracker implements EntityProvider, RemovalListener<Long, Entity> {
	private final Cache<Long, Entity> entities = CacheBuilder.newBuilder().removalListener(this).softValues().build();
	private final EntityLoader loader = new EntityLoader();
	
	@Override @SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(long uid) {
		try {
			return (T) entities.get(uid, loader);
		} catch (ExecutionException e) {
			throw new IllegalArgumentException("No entity with uid <" + uid + "> found", e);
		}
	}
	
	void addEntity(Entity entity) {
		entities.put(entity.uid, entity);
	}
	
	@Override
	public Collection<Entity> getEntities() {
		return entities.asMap().values();
	}
	
	short getModuleUID(Entity entity) {
		return getModuleUID(entity.uid);
	}
	
	short getModuleUID(long entity) {
		return (short) (entity >>> 48);		
	}

	@Override
	public void onRemoval(RemovalNotification<Long, Entity> notification) {
		System.out.println("entity removed from tracker");
	}
	
	private class EntityLoader implements Callable<Entity> {
		@Override
		public Entity call() {
			return null;
		}
	}
}
