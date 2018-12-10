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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import neon.common.entity.components.Component;

public final class Magic implements Component {
	private final long uid;
	private final Set<String> spells = new HashSet<>();
	
	private Optional<String> equipped = Optional.empty();
	
	public Magic(long uid) {
		this.uid = uid;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Magic:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	public void addSpell(String spell) {
		spells.add(Objects.requireNonNull(spell, "spell"));
	}
	
	public Set<String> getSpells() {
		return ImmutableSet.copyOf(spells);
	}
	
	public void equip(String spell) {
		if (spells.contains(spell)) {
			equipped = Optional.of(spell);
		}
	}
	
	public Optional<String> getEquipped() {
		return equipped;
	}
	
	public boolean hasEquipped(String spell) {
		return equipped.orElse("").equals(spell);
	}
	
	public void unequip() {
		equipped = Optional.empty();
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
}
