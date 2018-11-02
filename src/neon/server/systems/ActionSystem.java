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

import com.google.common.eventbus.EventBus;

import neon.common.entity.Entity;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.event.TimerEvent;
import neon.common.resources.CGame;
import neon.common.resources.CGame.GameMode;
import neon.common.resources.RMap;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

public final class ActionSystem implements NeonSystem {
	private static final long PLAYER_UID = 0;
	
	private final EntityManager entities;
	private final ResourceManager resources;
	private final EventBus bus;
	
	public ActionSystem(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.entities = entities;
		this.resources = resources;
		this.bus = bus;
	}

	public void run() throws ResourceException {
		// restore the player's action points
		CGame config = resources.getResource("config", "game");
		int fraction = config.getMode().equals(GameMode.REAL_TIME) ? 5 : 1;
		restore(entities.getEntity(PLAYER_UID), fraction);

		// restore all other creatures
		RMap map = resources.getResource("maps", config.getCurrentMap());
		map.getEntities().parallelStream()
				.<Entity>map(uid -> entities.getEntity(uid))
				.filter(entity -> entity.hasComponent(Stats.class))
				.forEach(entity -> restore(entity, fraction));
	}

	private void restore(Entity entity, int fraction) {
		Stats stats = entity.getComponent(Stats.class);
		stats.restoreAP(fraction);
		int health = stats.getHealth();
		int mana = stats.getMana();
		stats.addHealth(Math.min(stats.getBaseCon()/10, stats.getBaseHealth() - stats.getHealth())/fraction);
		stats.addMana(Math.min(stats.getBaseWis()/10, stats.getBaseMana() - stats.getMana())/fraction);
		// send updated stats to client if necessary
		if (health != stats.getHealth() || mana != stats.getMana()) {
			bus.post(new ComponentUpdateEvent(stats));
		}
	}

	@Override
	public void onTimerTick(TimerEvent tick) {
		
	}
}
