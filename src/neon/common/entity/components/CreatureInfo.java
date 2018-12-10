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

package neon.common.entity.components;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class CreatureInfo implements Component {
	private final String id;
	private final String name;
	private final long uid;
	private final Set<String> factions = new HashSet<>();
	
	public CreatureInfo(long uid, String id, String name) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.uid = uid;
	}
	
	@Override
	public String toString() {
		// create a string in module:map:entity format
		return "Creature:" + (uid >>> 48) + ":" + ((uid & 0x0000FFFF00000000l) >>> 32) + ":" + (uid & 0x00000000FFFFFFFFl);
	}
	
	public String getResource() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	public Set<String> getFactions() {
		return ImmutableSet.copyOf(factions);
	}
	
	public void addFaction(String faction) {
		factions.add(Objects.requireNonNull(faction, "faction"));
	}
	
	public boolean isMember(String faction) {
		return factions.contains(faction);
	}
}
