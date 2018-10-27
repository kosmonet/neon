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

package neon.systems.combat;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.Slot;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.resources.RItem;
import neon.server.entity.EntityManager;
import neon.util.Dice;

public class CombatSystem {
	private final EntityManager entities;
	private final EventBus bus;
	
	public CombatSystem(EntityManager entities, EventBus bus) {
		this.entities = entities;
		this.bus = bus;
		
		entities.addBuilder(RItem.Armor.class, new ArmorBuilder());
		entities.addBuilder(RItem.Weapon.class, new WeaponBuilder());
	}
	
	@Subscribe
	private void fight(CombatEvent.Start event) {
		Entity attacker = entities.getEntity(event.attacker);
		Entity defender = entities.getEntity(event.defender);
		
		Stats stats = defender.getComponent(Stats.class);
		int damage = Math.max(1, getDamage(attacker.getComponent(Inventory.class)) - getArmor(defender.getComponent(Inventory.class)));
		stats.addHealth(-damage);
		
		bus.post(new ComponentUpdateEvent(stats));
		bus.post(new CombatEvent.Result(event.attacker, event.defender, damage));
	}

	private int getDamage(Inventory inventory) {
		int damage = 0;

		if (inventory.hasEquiped(Slot.WEAPON)) {
			Entity item = entities.getEntity(inventory.getEquipedItem(Slot.WEAPON));
			if (item.hasComponent(Weapon.class)) {
				damage = Dice.roll(item.getComponent(Weapon.class).getDamage());
			}			
		}

		return damage;
	}

	private int getArmor(Inventory inventory) {
		int AC = 0;

		for (long uid : inventory.getEquipedItems()) {
			Entity item = entities.getEntity(uid);
			if (item.hasComponent(Armor.class)) {
				AC += item.getComponent(Armor.class).getRating();
			}
		}

		return AC;
	}
}
