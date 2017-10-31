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

import org.jdom2.Element;

import neon.entity.components.InventoryComponent;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Item;
import neon.entity.entities.Player;

class EntitySaver {
	Element save(Entity entity) {
		if (entity instanceof Player) {
			return savePlayer((Player) entity);
		} else if (entity instanceof Creature) {
			return saveCreature((Creature) entity);
		} else if (entity instanceof Item) {
			return saveItem((Item) entity);
		} else {
			throw new IllegalArgumentException("Unknown entity type");
		}
	}
	
	private Element savePlayer(Player player) {
		Element root = new Element("player");
		root.setAttribute("id", player.info.getResource().id);
		root.addContent(saveInventory(player.inventory));
		
//		Element position = new Element("position");
//		position.setAttribute("x", Integer.toString(player.shape.getX()));
//		position.setAttribute("y", Integer.toString(player.shape.getY()));
//		root.addContent(position);
		
		return root;			
	}

	private Element saveCreature(Creature creature) {
		Element root = new Element("creature");
		root.setAttribute("id", creature.info.getResource().id);
		root.addContent(saveInventory(creature.inventory));
		return root;			
	}

	private Element saveItem(Item item) {
		Element root = new Element("item");
		root.setAttribute("id", item.info.getResource().id);
		return root;			
	}
	
	private Element saveInventory(InventoryComponent items) {
		Element inventory = new Element("items");
		for(Long uid : items.getItems()) {
			Element item = new Element("item");
			item.setAttribute("uid", Long.toString(uid));
			inventory.addContent(item);
		}
		return inventory;
	}
}
