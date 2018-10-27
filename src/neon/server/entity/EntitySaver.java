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

package neon.server.entity;

import org.jdom2.Element;

import neon.common.entity.Entity;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Shape;
import neon.common.resources.RCreature;
import neon.common.resources.RItem;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

public class EntitySaver {
	private final static long PLAYER_UID = 0;
	
	private final ResourceManager resources;
	private final CreatureBuilder creatureBuilder = new CreatureBuilder();
	private final ItemBuilder itemBuilder = new ItemBuilder();
	
	public EntitySaver(ResourceManager resources) {
		this.resources = resources;
	}
	
	Element save(Entity entity) {
		if (entity.hasComponent(PlayerInfo.class)) {
			return savePlayer(entity);
		} else if (entity.hasComponent(CreatureInfo.class)) {
			return saveCreature(entity);
		} else if (entity.hasComponent(ItemInfo.class)) {
			return saveItem(entity);
		} else {
			throw new IllegalArgumentException("Unknown entity type");
		}
	}
	
	private Element savePlayer(Entity player) {
		Element root = new Element("player");
		CreatureInfo info = player.getComponent(CreatureInfo.class);
		root.setAttribute("id", info.getResource());
		root.addContent(saveInventory(player.getComponent(Inventory.class)));
		PlayerInfo record = player.getComponent(PlayerInfo.class);
		root.setAttribute("name", record.getName());
		
		Shape shape = player.getComponent(Shape.class);
		root.setAttribute("x", Integer.toString(shape.getX()));
		root.setAttribute("y", Integer.toString(shape.getY()));
		return root;			
	}

	private Element saveCreature(Entity creature) {
		Element root = new Element("creature");
		CreatureInfo info = creature.getComponent(CreatureInfo.class);
		root.setAttribute("id", info.getResource());
		root.addContent(saveInventory(creature.getComponent(Inventory.class)));

		Shape shape = creature.getComponent(Shape.class);
		root.setAttribute("x", Integer.toString(shape.getX()));
		root.setAttribute("y", Integer.toString(shape.getY()));		
		return root;			
	}

	private Element saveItem(Entity item) {
		Element root = new Element("item");
		ItemInfo info = item.getComponent(ItemInfo.class);
		root.setAttribute("id", info.getResource());
		return root;			
	}
	
	private Element saveInventory(Inventory items) {
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
			return itemBuilder.build(uid, item);
		} else if (root.getName().equals("creature")) {
			RCreature species = resources.getResource("creatures", id);
			Entity creature = creatureBuilder.build(PLAYER_UID, species);
			Shape shape = creature.getComponent(Shape.class);
			shape.setX(Integer.parseInt(root.getAttributeValue("x")));
			shape.setY(Integer.parseInt(root.getAttributeValue("y")));
			return creature;
		} else if (root.getName().equals("player")) {
			RCreature species = resources.getResource("creatures", id);
			Entity player = creatureBuilder.build(PLAYER_UID, species);
			player.setComponent(new PlayerInfo(0, root.getAttributeValue("name"), "gender"));
			Shape shape = player.getComponent(Shape.class);
			shape.setX(Integer.parseInt(root.getAttributeValue("x")));
			shape.setY(Integer.parseInt(root.getAttributeValue("y")));
			loadInventory(root.getChild("items"), player.getComponent(Inventory.class));
			return player;
		} else {
			throw new IllegalArgumentException("Entity <" + uid + "> does not exist");
		}
	}
	
	private void loadInventory(Element items, Inventory inventory) {
		for (Element item : items.getChildren()) {
			inventory.addItem(Long.parseLong(item.getAttributeValue("uid")));
		}
	}
}
