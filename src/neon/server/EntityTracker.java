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
import java.util.HashMap;
import java.util.Map;

import neon.common.resources.CServer;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.entities.Entity;

/**
 * This class tracks all loaded entities in the current game (and by extension
 * all loaded modules and maps). 
 * 
 * @author mdriesen
 *
 */
public class EntityTracker implements EntityProvider {
	private final Map<Long, Entity> entities = new HashMap<>();
	private final MapLoader loader;
	private final CServer config;

	EntityTracker(ResourceManager resources, CServer config) {
		loader = new MapLoader(this, resources);
		resources.addLoader("map", loader);
		resources.setAutoSave("map", true);
		this.config = config;
	}
	
	@Override @SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(long uid) {
		return (T) entities.get(uid);
	}
	
	void addEntity(Entity entity) {
		entities.put(entity.uid, entity);
	}
	
	@Override
	public Collection<Entity> getEntities() {
		return entities.values();
	}
	
	short getModuleUID(Entity entity) {
		return getModuleUID(entity.uid);
	}
	
	short getModuleUID(long entity) {
		return (short) (entity >>> 48);		
	}
	
	/**
	 * Calculates the full map uid given the module name and the base uid
	 * of the map within the module.
	 * 
	 * @param uid
	 * @param module
	 * @return
	 */
	int getMapUID(short base, String module) {
		return ((int)config.getModuleUID(module) << 16) | base;
	}
}
