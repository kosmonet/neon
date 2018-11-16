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

import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.Stats;
import neon.common.event.ComponentUpdateEvent;
import neon.common.files.NeonFileSystem;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.server.entity.EntityManager;

/**
 * The system that takes care of spells and enchantments.
 * 
 * @author mdriesen
 *
 */
public final class MagicSystem {
	private static final Logger logger = Logger.getGlobal();
	private static final long PLAYER_UID = 0;
	
	private final EventBus bus;
	private final EntityManager entities;
	private final ResourceManager resources;
	
	public MagicSystem(NeonFileSystem files, ResourceManager resources, EntityManager entities, EventBus bus) {
		this.bus = bus;
		this.entities = entities;
		this.resources = resources;

		resources.addLoader(new SpellLoader(files));
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

	/**
	 * Handles spellcasting events.
	 * 
	 * @param event
	 * @throws ResourceException
	 */
	@Subscribe
	private void onCast(MagicEvent.Cast event) throws ResourceException {
		RSpell spell = resources.getResource("spells", event.spell);
		cast(event.target, spell.effect, spell.magnitude);
		Stats casterStats = entities.getEntity(event.caster).getComponent(Stats.class);
		casterStats.addMana(-MagicUtils.getCost(spell));
		bus.post(new ComponentUpdateEvent(casterStats));
	}
	
	/**
	 * Handles enchantment events.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onItemUse(MagicEvent.Use event) {
		Entity item = entities.getEntity(event.item);
		Enchantment enchantment = item.getComponent(Enchantment.class);
		cast(PLAYER_UID, enchantment.getEffect(), enchantment.getMagnitude());
	}
	
	private void cast(long target, Effect effect, int magnitude) {
		Stats stats = entities.getEntity(target).getComponent(Stats.class);
		switch (effect) {
		case HEAL:
			stats.addHealth(magnitude);
			bus.post(new ComponentUpdateEvent(stats));
			break;
		case FREEZE:
			stats.addHealth(-magnitude);
			bus.post(new ComponentUpdateEvent(stats));
			break;
		case BURN:
			stats.addHealth(-magnitude);
			bus.post(new ComponentUpdateEvent(stats));
			break;
		default:
			logger.warning("unknown spell effect: " + effect);
			break;
		}		
	}
}
