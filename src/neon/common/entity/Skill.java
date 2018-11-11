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

package neon.common.entity;

public enum Skill {
	// combat
	BLADE(10, "Blade"), BLOCK(10, "Block"), HEAVY_ARMOR(10, "Heavy armor"), 
	MEDIUM_ARMOR(10, "Medium armor"), LIGHT_ARMOR(10, "Light armor"),
	
	// stealth
	PICKPOCKET(10, "Pickpocket"), LOCKPICKING(10, "Lockpicking"),
	
	// magic
	DESTRUCTION(10, "Destruction"), RESTORATION(10, "Restoration"), 
	ALTERATION(10, "Alteration"), ILLUSION(10, "Illusion"), CONJURATION(10, "Conjuration"),
	
	// other
	SWIMMING(10, "Swimming");
	
	public final int steps;
	private final String name;
	
	private Skill(int steps, String name) {
		this.steps = steps;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
