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

package neon.common.event;

import java.util.Objects;

/**
 * Event to signal that a new game should be started.
 * 
 * @author mdriesen
 *
 */
public abstract class NewGameEvent extends NeonEvent {
	public static final class Check extends NewGameEvent {
		public final String name, species, gender;
		public final int strength, constitution, dexterity, intelligence, wisdom, charisma;

		public Check(String name, String species, String gender, int strength, int constitution, int dexterity, int intelligence, int wisdom, int charisma) {
			this.name = Objects.requireNonNull(name, "name");
			this.species = Objects.requireNonNull(species, "species");
			this.gender = Objects.requireNonNull(gender, "gender");

			this.strength =  strength;
			this.constitution = constitution;
			this.dexterity = dexterity;
			this.intelligence = intelligence;
			this.wisdom = wisdom;
			this.charisma = charisma;
		}
	}
	
	public static final class Pass extends NewGameEvent {}
	public static final class Fail extends NewGameEvent {}
}
