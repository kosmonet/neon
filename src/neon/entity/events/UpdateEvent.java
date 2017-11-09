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

package neon.entity.events;

import java.util.Collection;
import java.util.HashSet;

import neon.common.event.NeonEvent;
import neon.common.resources.RMap;
import neon.common.resources.Resource;
import neon.entity.entities.Entity;
import neon.util.spatial.RegionQuadTree;
import neon.util.spatial.RegionSpatialIndex;

/**
 * An event containing updates for the client.
 * 
 * @author mdriesen
 *
 */
public abstract class UpdateEvent extends NeonEvent {
	protected final Collection<Resource> resources = new HashSet<>();
	protected final Collection<Entity> entities = new HashSet<>();
	
	public Collection<Resource> getResources() {
		return resources;
	}
	
	public Collection<Entity> getEntities() {
		return entities;
	}
	
	/**
	 * An event to indicate that a game is started.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Start extends UpdateEvent {}
	
	/**
	 * An event to indicate a change of map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Map extends UpdateEvent {
		private final RMap map;
		
		public Map(RMap map, Collection<Resource> resources, Collection<Entity> entities) {
			this.map = map;
			this.resources.addAll(resources);
			this.entities.addAll(entities);
		}
		
		public RegionQuadTree<String> getTerrain() {
			return map.getTerrain();
		}
		
		public RegionSpatialIndex<Integer> getElevation() {
			return map.getElevation();
		}
		
		public RMap getMap() {
			return map;
		}
	}
	
	/**
	 * An event to signal an entity update.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Entities extends UpdateEvent {
		public Entities(Collection<Entity> entities) {
			this.entities.addAll(entities);
		}
		
		public Entities(Entity entity) {
			entities.add(entity);
		}
	}
}
