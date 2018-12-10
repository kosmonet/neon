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

package neon.systems.combat;

import java.util.Objects;

import neon.common.entity.Entity;
import neon.common.entity.components.Equipment;
import neon.common.resources.Slot;
import neon.server.entity.EntityManager;
import neon.util.Dice;

public class SimpleDamageStrategy implements DamageStrategy {
	private final EntityManager entities;

	public SimpleDamageStrategy(EntityManager entities) {
		this.entities = Objects.requireNonNull(entities, "entity manager");
	}
	
	@Override
	public int getDamage(Entity attacker, Entity defender) {
		int damage = Math.max(1, getDamage(attacker.getComponent(Equipment.class)) - getArmor(defender.getComponent(Equipment.class)));
		return damage;
	}
	
	private int getDamage(Equipment equipment) {
		int damage = 0;

		// creatures can have weapons equipped in both hands
		if (equipment.hasEquipped(Slot.HAND_LEFT)) {
			Entity item = entities.getEntity(equipment.getEquipedItem(Slot.HAND_LEFT));
			if (item.hasComponent(Weapon.class)) {
				damage += Dice.roll(item.getComponent(Weapon.class).getDamage());
			}			
		}

		if (equipment.hasEquipped(Slot.HAND_RIGHT)) {
			Entity item = entities.getEntity(equipment.getEquipedItem(Slot.HAND_RIGHT));
			if (item.hasComponent(Weapon.class)) {
				damage += Dice.roll(item.getComponent(Weapon.class).getDamage());
			}			
		}

		return damage;
	}

	private int getArmor(Equipment equipment) {
		int AC = 0;

		for (long uid : equipment.getEquippedItems()) {
			Entity item = entities.getEntity(uid);
			if (item.hasComponent(Armor.class)) {
				AC += item.getComponent(Armor.class).getRating();
			}
		}

		return AC;
	}
}
