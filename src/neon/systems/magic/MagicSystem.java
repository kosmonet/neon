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

package neon.systems.magic;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

public class MagicSystem {
	private final static long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final EntityManager entities;
	private final ResourceManager resources;
	
	public MagicSystem(ResourceManager resources, EntityManager entities, EventBus bus) {
		this.bus = bus;
		this.entities = entities;
		this.resources = resources;

		resources.addLoader("spells", new SpellLoader());
	}
	
	@Subscribe
	private void onSpellEquip(MagicEvent.Equip event) {
		Magic magic = entities.getEntity(PLAYER_UID).getComponent(Magic.class);
		magic.equip(event.spell);
		bus.post(new ComponentUpdateEvent(magic));
	}

	@Subscribe
	private void onSpellUnequip(MagicEvent.Unequip event) {
		Magic magic = entities.getEntity(PLAYER_UID).getComponent(Magic.class);
		magic.unequip();
		bus.post(new ComponentUpdateEvent(magic));
	}

	@Subscribe
	private void onCast(MagicEvent.Cast event) throws ResourceException {
//		Magic magic = entities.getEntity(PLAYER_UID).getComponent(Magic.class);
		RSpell spell = resources.getResource("spells", event.spell);
		
		switch (spell.effect) {
		case HEAL:
			Stats stats = entities.getEntity(event.target).getComponent(Stats.class);
			stats.addHealth(spell.magnitude);
			bus.post(new ComponentUpdateEvent(stats));
			break;
		}
	}
}
