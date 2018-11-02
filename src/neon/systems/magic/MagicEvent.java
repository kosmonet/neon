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

import neon.common.event.NeonEvent;

public abstract class MagicEvent extends NeonEvent {
	public static final class Equip extends MagicEvent {
		public final String spell;
		
		public Equip(String spell) {
			this.spell = spell;
		}
	}
	
	public static final class Unequip extends MagicEvent {
		public final String spell;
		
		public Unequip(String spell) {
			this.spell = spell;
		}
	}
	
	public static final class Cast extends MagicEvent {
		public final String spell;
		public final long target;
		public final long caster;
		
		public Cast(long caster, String spell, long target) {
			this.spell = spell;
			this.target = target;
			this.caster = caster;
		}
	}
	
	public static final class Use extends MagicEvent {
		public final long item;
		
		public Use(long item) {
			this.item = item;
		}
	}
}
