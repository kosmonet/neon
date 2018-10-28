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

package neon.common.entity.components;

import neon.common.entity.PlayerMode;

/**
 * Component that contains all player-specific information.
 * 
 * @author mdriesen
 *
 */
public class PlayerInfo implements Component {
	private final long uid;
	private final String name;
	private final String gender;
	
	private PlayerMode mode = PlayerMode.NONE;
	
	public PlayerInfo(long uid, String name, String gender) {
		this.uid = uid;
		this.name = name;
		this.gender = gender;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Player:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setMode(PlayerMode mode) {
		this.mode = mode;
	}
	
	public PlayerMode getMode() {
		return mode;
	}
}
