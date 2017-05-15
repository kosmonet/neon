/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

package neon.system.event;

/**
 * Event to signal that a new game should be started.
 * 
 * @author mdriesen
 *
 */
public class NewGameEvent extends ServerEvent {
	private final String name, species, gender;
	
	public NewGameEvent(String name, String species, String gender) {
		this.name = name;
		this.species = species;
		this.gender = gender;
	}
	
	public String getName() {
		return name;
	}

	public String getSpecies() {
		return species;
	}

	public String getGender() {
		return gender;
	}
}
