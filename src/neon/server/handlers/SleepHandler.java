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
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentEvent;
import neon.server.entity.EntityManager;
import neon.systems.time.RestEvent;

/**
 * A handler for sleeping and resting.
 * 
 * @author mdriesen
 *
 */
public final class SleepHandler {
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final EntityManager entities;
	
	/**
	 * 
	 * @param entities
	 * @param bus
	 */
	public SleepHandler(EntityManager entities, EventBus bus) {
		this.entities = Objects.requireNonNull(entities, "entity manager");
		this.bus = Objects.requireNonNull(bus, "event bus");
	}
	
	/**
	 * Handles sleep events.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onSleep(RestEvent.Sleep event) {
		Entity player = entities.getEntity(PLAYER_UID);
		Stats stats = player.getComponent(Stats.class);
		stats.addHealth(stats.getBaseHealth() - stats.getHealth());
		stats.addMana(stats.getBaseMana() - stats.getMana());
		bus.post(new RestEvent.Wake());
		bus.post(new ComponentEvent(stats));
	}
}
