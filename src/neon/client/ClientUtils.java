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

package neon.client;

import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;

/**
 * Utility methods for the client.
 * 
 * @author mdriesen
 */
public final class ClientUtils {
	// suppress default constructor for noninstantiability
	private ClientUtils() {
		throw new AssertionError();
	}
	
	/**
	 * Calculates the total weight of all items in an inventory.
	 * 
	 * @param inventory
	 * @param components
	 * @return
	 */
	public static int getWeight(Inventory inventory, ComponentManager components) {
		int weight = 0;
		for (long uid : inventory.getItems()) {
			weight += components.getComponent(uid, ItemInfo.class).weight;
		}
		return weight/100;
	}	
}
