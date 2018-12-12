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

package neon.common.entity;

/**
 * The different play styles a player can choose during the game. These mainly
 * influence the default actions taken when interacting with other creatures.
 * 
 * @author mdriesen
 *
 */
public enum PlayerMode {
	/** Stealth mode. In this mode, the player has the option to pickpocket. */
	STEALTH("sneaky"), 
	/** Aggressive mode. In this mode, the player can attack friendly creatures. */
	AGGRESSION("aggressive"), 
	/** Normal mode. */
	NONE("normal");

	private String name;
	
	private PlayerMode(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
