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

import neon.common.event.NeonEvent;

public abstract class CombatEvent extends NeonEvent {
	public final long attacker;
	public final long defender;
	
	private CombatEvent(long attacker, long defender) {
		this.attacker = attacker;
		this.defender = defender;
	}
	
	public final static class Start extends CombatEvent {
		public Start(long attacker, long defender) {
			super(attacker, defender);
		}		
	}
	
	public final static class Result extends CombatEvent {
		public final long damage;
		
		public Result(long attacker, long defender, int damage) {
			super(attacker, defender);
			this.damage = damage;
		}
	}
}
