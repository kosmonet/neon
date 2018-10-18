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

import neon.common.event.CombatEvent;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.TimerEvent;
import neon.common.resources.RItem;
import neon.common.resources.Resource;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.Slot;
import neon.entity.components.Inventory;
import neon.entity.components.Stats;
import neon.entity.entities.Creature;
import neon.entity.entities.Item;
import neon.util.Dice;

public class CombatSystem implements NeonSystem {
	private final EntityProvider entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public CombatSystem(ResourceManager resources, EntityProvider entities, EventBus bus) {
		this.entities = entities;
		this.resources = resources;
		this.bus = bus;
	}
	
	@Subscribe
	private void fight(CombatEvent.Start event) throws ResourceException {
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
	
	private int getDamage(Inventory inventory) throws ResourceException {
		int damage = 0;
		
		if (inventory.hasEquiped(Slot.WEAPON)) {
			Item item = entities.getEntity(inventory.getEquipedItem(Slot.WEAPON));
			String id = item.getComponent(Item.Resource.class).getID();
			Resource resource = resources.getResource(id);
			if (resource instanceof RItem.Weapon) {
				RItem.Weapon weapon = (RItem.Weapon) resource;
				damage = Dice.roll(weapon.damage);
			}			
		}
		
		return damage;
	}
	
	private int getArmor(Inventory inventory) throws ResourceException {
		int AC = 0;
		
		for (long uid : inventory.getEquipedItems()) {
			Item item = entities.getEntity(uid);
			String id = item.getComponent(Item.Resource.class).getID();
			Resource resource = resources.getResource(id);
			if (resource instanceof RItem.Armor) {
				RItem.Armor armor = (RItem.Armor) resource;
				AC += armor.rating;
			}
		}
		
		return AC;
	}
}
