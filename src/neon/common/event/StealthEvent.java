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

package neon.common.event;

public abstract class StealthEvent extends NeonEvent {
	public static final class Pick extends StealthEvent {
		public final long victim;
		
		public Pick(long victim) {
			this.victim = victim;
		}
	}
	
	public static final class Unlock extends StealthEvent {
		public final long lock;
		
		public Unlock(long lock) {
			this.lock = lock;
		}
	}
	
	public static final class Empty extends StealthEvent {}
	public static final class Stolen extends StealthEvent {}
	public static final class Unlocked extends StealthEvent {}
}
