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

package neon.server.handlers;

import java.util.Objects;

import com.google.common.eventbus.EventBus;

import neon.common.entity.Entity;
import neon.common.entity.components.Clothing;
import neon.common.entity.components.CreatureInfo;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.Equipment;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.ItemInfo;
import neon.common.entity.components.Lock;
import neon.common.entity.components.PlayerInfo;
import neon.common.entity.components.Provider;
import neon.common.entity.components.Shape;
import neon.common.entity.components.Skills;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentEvent;
import neon.common.event.UpdateEvent;
import neon.server.entity.EntityManager;
import neon.server.entity.Map;
import neon.systems.ai.Behavior;
import neon.systems.combat.Armor;
import neon.systems.combat.Weapon;
import neon.systems.magic.Enchantment;
import neon.systems.magic.Magic;

/**
 * A helper class for sending entity updates to the server via the event bus.
 * 
 * @author mdriesen
 *
 */
final class Notifier {
	private final EventBus bus;
	private final EntityManager entities;

	/**
	 * Initialize a new notifier. The entity manager and event bus must not be
	 * null.
	 * 
	 * @param entities
	 * @param bus
	 */
	Notifier(EntityManager entities, EventBus bus) {
		this.entities = Objects.requireNonNull(entities, "entity manager");
		this.bus = Objects.requireNonNull(bus, "event bus");
	}
	
	/**
	 * Notifies the client that the player entity was updated.
	 * 
	 * @param player
	 */
	void notifyClient(Entity player) {
		Inventory inventory = player.getComponent(Inventory.class);
		inventory.getItems().parallelStream().map(entities::getEntity).forEach(this::notifyItem);
		bus.post(new ComponentEvent(inventory));
		bus.post(new ComponentEvent(player.getComponent(Stats.class)));
		bus.post(new ComponentEvent(player.getComponent(Skills.class)));
		bus.post(new ComponentEvent(player.getComponent(Magic.class)));
		bus.post(new ComponentEvent(player.getComponent(CreatureInfo.class)));
		bus.post(new ComponentEvent(player.getComponent(Graphics.class)));
		bus.post(new ComponentEvent(player.getComponent(Shape.class)));
		bus.post(new ComponentEvent(player.getComponent(PlayerInfo.class)));
		bus.post(new ComponentEvent(player.getComponent(Equipment.class)));
	}
		
	/**
	 * Notifies the client that a new map was loaded.
	 * 
	 * @param map
	 */
	void notifyClient(Map map) {
		// then send the map
		bus.post(new UpdateEvent.Map(map.getUid(), map.getId()));

		for (long uid : map.getEntities()) {
			Entity entity = entities.getEntity(uid);
			Shape shape = entity.getComponent(Shape.class);
			if (entity.hasComponent(CreatureInfo.class)) {
				notifyCreature(entity);
				bus.post(new UpdateEvent.Move(uid, map.getUid(), shape.getX(), shape.getY(), shape.getZ()));
			} else if (entity.hasComponent(ItemInfo.class)) {
				notifyItem(entity);
				bus.post(new UpdateEvent.Move(uid, map.getUid(), shape.getX(), shape.getY(), shape.getZ()));
			}
		}		
	}
	
	/**
	 * Notifies the client of a new creature.
	 * 
	 * @param creature
	 */
	private void notifyCreature(Entity creature) {
		Inventory inventory = creature.getComponent(Inventory.class);
		inventory.getItems().parallelStream().map(entities::getEntity).forEach(this::notifyItem);
		bus.post(new ComponentEvent(creature.getComponent(Behavior.class)));
		bus.post(new ComponentEvent(creature.getComponent(CreatureInfo.class)));
		bus.post(new ComponentEvent(creature.getComponent(Graphics.class)));
		bus.post(new ComponentEvent(creature.getComponent(Magic.class)));
		bus.post(new ComponentEvent(creature.getComponent(Equipment.class)));
		if (creature.hasComponent(Provider.class)) {
			bus.post(new ComponentEvent(creature.getComponent(Provider.class)));			
		}
	}
	
	/**
	 * Notifies the client of a new item.
	 * 
	 * @param item
	 */
	private void notifyItem(Entity item) {
		bus.post(new ComponentEvent(item.getComponent(ItemInfo.class)));
		bus.post(new ComponentEvent(item.getComponent(Graphics.class)));
		
		if (item.hasComponent(Clothing.class)) {
			bus.post(new ComponentEvent(item.getComponent(Clothing.class)));
			if (item.hasComponent(Armor.class)) {
				bus.post(new ComponentEvent(item.getComponent(Armor.class)));
			}
		} else if (item.hasComponent(Weapon.class)) {
			bus.post(new ComponentEvent(item.getComponent(Weapon.class)));
		}
		
		if (item.hasComponent(Enchantment.class)) {
			bus.post(new ComponentEvent(item.getComponent(Enchantment.class)));
		}

		if (item.hasComponent(Lock.class)) {
			bus.post(new ComponentEvent(item.getComponent(Lock.class)));
		}

		if (item.hasComponent(DoorInfo.class)) {
			bus.post(new ComponentEvent(item.getComponent(DoorInfo.class)));
		}

		if (item.hasComponent(Inventory.class)) {
			Inventory inventory = item.getComponent(Inventory.class);
			inventory.getItems().parallelStream().map(entities::getEntity).forEach(this::notifyItem);
			bus.post(new ComponentEvent(item.getComponent(Inventory.class)));
		}

		if (item.hasComponent(DoorInfo.class)) {
			bus.post(new ComponentEvent(item.getComponent(DoorInfo.class)));
		}
	}
}
