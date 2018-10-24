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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.EntityProvider;
import neon.common.entity.Slot;
import neon.common.entity.components.Armor;
import neon.common.entity.components.Inventory;
import neon.common.entity.components.Stats;
import neon.common.entity.components.Weapon;
import neon.common.entity.entities.Creature;
import neon.common.entity.entities.Item;
import neon.common.event.CombatEvent;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.TimerEvent;
import neon.util.Dice;

public class CombatSystem implements NeonSystem {
	private final EntityProvider entities;
	private final EventBus bus;
	
	public CombatSystem(EntityProvider entities, EventBus bus) {
		this.entities = entities;
		this.bus = bus;
	}
	
	@Subscribe
	private void fight(CombatEvent.Start event) {
		Creature attacker = entities.getEntity(event.attacker);
		Creature defender = entities.getEntity(event.defender);
		
		Stats stats = defender.getComponent(Stats.class);
		int damage = Math.max(1, getDamage(attacker.getComponent(Inventory.class)) - getArmor(defender.getComponent(Inventory.class)));
		stats.addHealth(-damage);
		
		bus.post(new ComponentUpdateEvent(stats));
		bus.post(new CombatEvent.Result(event.attacker, event.defender, damage));
	}

	@Override
	public void onTimerTick(TimerEvent tick) {}
	
	private int getDamage(Inventory inventory) {
		int damage = 0;

		if (inventory.hasEquiped(Slot.WEAPON)) {
			Item item = entities.getEntity(inventory.getEquipedItem(Slot.WEAPON));
			if (item.hasComponent(Weapon.class)) {
				damage = Dice.roll(item.getComponent(Weapon.class).getDamage());
			}			
		}

		return damage;
	}

	private int getArmor(Inventory inventory) {
		int AC = 0;

		for (long uid : inventory.getEquipedItems()) {
			Item item = entities.getEntity(uid);
			if (item.hasComponent(Armor.class)) {
				AC += item.getComponent(Armor.class).getRating();
			}
		}

		return AC;
	}
}
