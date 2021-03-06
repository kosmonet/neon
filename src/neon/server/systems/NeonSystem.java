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

package neon.server.systems;

import java.util.Optional;

import neon.common.entity.Entity;

/**
 * A system that can be added to the system manager.
 * 
 * @author mdriesen
 *
 */
public interface NeonSystem extends Runnable {
	/**
	 * Updates an entity. If no further updates are necessary on this or any other 
	 * entity, implementations should return an empty optional. If further updates 
	 * are necessary, implementations should return an optional containing
	 * the entity.
	 * 
	 * @param entity
	 * @return
	 */
	public default Optional<Entity> update(Entity entity) {
		return Optional.empty();
	}
	
	public default void run() {}
}
