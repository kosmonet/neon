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

package neon.common.entity.components;

import java.util.EnumSet;
import java.util.Set;

public class Provider implements Component {
	public enum Service {
		HEALER, TRADE, SPELLS, ROOMS;
	}
	
	private final long uid;
	private final Set<Service> services = EnumSet.noneOf(Service.class);
	
	public Provider(long uid) {
		this.uid = uid;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Service:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	/**
	 * Checks whether a service is provided.
	 * 
	 * @param service
	 * @return
	 */
	public boolean hasService(Service service) {
		return services.contains(service);
	}
	
	/**
	 * Adds a service.
	 * 
	 * @param service
	 */
	public void addService(Service service) {
		services.add(service);
	}
}
