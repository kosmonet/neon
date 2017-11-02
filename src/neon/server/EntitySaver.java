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

import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.components.InventoryComponent;
import neon.entity.entities.Creature;
import neon.entity.entities.Entity;
import neon.entity.entities.Item;
import neon.entity.entities.Player;

class EntitySaver {
	private final ResourceManager resources;
	
	EntitySaver(ResourceManager resources) {
		this.resources = resources;
	}
	
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
		root.setAttribute("name", player.record.getName());
		
		root.setAttribute("x", Integer.toString(player.shape.getX()));
		root.setAttribute("y", Integer.toString(player.shape.getY()));
		return root;			
	}

	private Element saveCreature(Creature creature) {
		Element root = new Element("creature");
		root.setAttribute("id", creature.info.getResource().id);
		root.addContent(saveInventory(creature.inventory));

		root.setAttribute("x", Integer.toString(creature.shape.getX()));
		root.setAttribute("y", Integer.toString(creature.shape.getY()));		
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
	
	Entity load(long uid, Element root) throws ResourceException {
		String id = root.getAttributeValue("id");
		
		if (root.getName().equals("item")) {
			RItem item = resources.getResource("items", id);
			return new Item(uid, item);
		} else if (root.getName().equals("creature")) {
			RCreature species = resources.getResource("creatures", id);
			Creature creature = new Creature(uid, species);
			creature.shape.setX(Integer.parseInt(root.getAttributeValue("x")));
			creature.shape.setY(Integer.parseInt(root.getAttributeValue("y")));
			return creature;
		} else if (root.getName().equals("player")) {
			RCreature species = resources.getResource("creatures", id);
			Player player = new Player(root.getAttributeValue("name"), "gender", species);
			player.shape.setX(Integer.parseInt(root.getAttributeValue("x")));
			player.shape.setY(Integer.parseInt(root.getAttributeValue("y")));
			loadInventory(root.getChild("items"), player.inventory);
			return player;
		} else {
			throw new IllegalArgumentException("Entity <" + uid + "> does not exist");
		}
	}
	
	private void loadInventory(Element items, InventoryComponent inventory) {
		for (Element item : items.getChildren()) {
			inventory.addItem(Long.parseLong(item.getAttributeValue("uid")));
		}
	}
}
