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

import java.util.HashSet;
import java.util.Set;

import neon.common.entity.components.Component;

public class Magic implements Component {
	private final long uid;
	private final Set<String> spells = new HashSet<>();
	
	public Magic(long uid) {
		this.uid = uid;
	}
	
	public void addSpell(String spell) {
		spells.add(spell);
	}
	
	public Set<String> getSpells() {
		return spells;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
}