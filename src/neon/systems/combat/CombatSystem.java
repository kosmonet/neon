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

import java.util.Objects;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.resources.RItem;
import neon.server.entity.EntityManager;

/**
 * The system that handles all combat-related activities.
 * 
 * @author mdriesen
 *
 */
public final class CombatSystem {
	private final DodgeStrategy dodgeStrategy;
	private final BlockStrategy blockStrategy;
	private final DamageStrategy damageStrategy;
	
	private final EntityManager entities;
	private final EventBus bus;
	
	public CombatSystem(EntityManager entities, EventBus bus) {
		this.bus = Objects.requireNonNull(bus, "event bus");
		this.entities = entities;
		
		entities.addBuilder(RItem.Armor.class, new ArmorBuilder());
		entities.addBuilder(RItem.Weapon.class, new WeaponBuilder());
		
		dodgeStrategy = new SimpleDodgeStrategy();
		blockStrategy = new SimpleBlockStrategy();
		damageStrategy = new SimpleDamageStrategy(entities);
	}
	
	@Subscribe
	private void fight(CombatEvent.Start event) {
		Entity attacker = entities.getEntity(event.attacker);
		Entity defender = entities.getEntity(event.defender);
		
		// check if it's a hit or miss
		if (dodgeStrategy.checkDodge(attacker, defender)) {
			bus.post(new CombatEvent.Dodge(attacker.uid, defender.uid));
		} else if (blockStrategy.checkBlock(attacker, defender)) {
			bus.post(new CombatEvent.Block(attacker.uid, defender.uid));			
		} else {
			Stats stats = defender.getComponent(Stats.class);
			int damage = damageStrategy.getDamage(attacker, defender);
			stats.addHealth(-damage);
			
			bus.post(new ComponentUpdateEvent(stats));
			bus.post(new CombatEvent.Result(attacker.uid, defender.uid, damage));			
		}
	}
}
