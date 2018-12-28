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

package neon.server.systems;

import java.util.Objects;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import neon.common.entity.Entity;
import neon.common.entity.components.Stats;
import neon.common.entity.components.Task;
import neon.common.event.ComponentEvent;

/**
 * The system that handles action points.
 * 
 * @author mdriesen
 *
 */
public final class ActionSystem implements NeonSystem {
	private final EventBus bus;
	
	/**
	 * The event bus must not be null.
	 * 
	 * @param bus
	 */
	public ActionSystem(EventBus bus) {
		this.bus = Objects.requireNonNull(bus, "event bus");
	}

	@Override
	public Optional<Entity> update(Entity creature) {
		if (creature.hasComponent(Task.Action.class)) {
			// if the creature has an action task, we add action points
			restore(creature, creature.getComponent(Task.Action.class).fraction);
			creature.removeComponent(Task.Action.class);
			Stats stats = creature.getComponent(Stats.class);
			if (stats.isActive()) {
				// if the creature has enough action points, we return the creature
				creature.setComponent(new Task.Think(creature.uid));
				return Optional.of(creature);
			} else {
				// if not, the creature is out
				return Optional.empty();
			}
		} else {
			// if the creature had no action task, we reschedule it
			return Optional.of(creature);
		}
	}
	
	/**
	 * Restores an entity's action points.
	 * 
	 * @param entity
	 * @param fraction
	 */
	private void restore(Entity entity, int fraction) {
		Stats stats = entity.getComponent(Stats.class);
		stats.restoreAP(fraction);
		int health = stats.getHealth();
		int mana = stats.getMana();
		stats.addHealth(Math.min(stats.getBaseCon()/10, stats.getBaseHealth() - stats.getHealth())/fraction);
		stats.addMana(Math.min(stats.getBaseWis()/10, stats.getBaseMana() - stats.getMana())/fraction);
		// send updated stats to client if necessary
		if (health != stats.getHealth() || mana != stats.getMana()) {
			bus.post(new ComponentEvent(stats));
		}
	}
}
