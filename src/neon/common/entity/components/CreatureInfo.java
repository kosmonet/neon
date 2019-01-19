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

/**
 * A component containing creature-specific information.
 * 
 * @author mdriesen
 *
 */
public final class CreatureInfo implements Component {
	private final String id;
	private final String name;
	private final long uid;
	private final Set<String> factions = new HashSet<>();
	
	/**
	 * The id and name must not be null.
	 * 
	 * @param uid	the uid of the creature
	 * @param id	the id of the creature resource the creature was based on
	 * @param name	the name of the creature
	 */
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
	
	/**
	 * Returns the species a creature belongs to.
	 * 
	 * @return	the id of a creature resource
	 */
	public String getResource() {
		return id;
	}

	/**
	 * Returns the name of a creature.
	 * 
	 * @return	the name
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
	
	/**
	 * Returns all factions a creature belongs to.
	 * 
	 * @return	an unmodifiable {@code Set<String>} of faction id's
	 */
	public Set<String> getFactions() {
		return ImmutableSet.copyOf(factions);
	}
	
	/**
	 * Adds a creature to a faction.
	 * 
	 * @param faction	the faction id
	 */
	public void addFaction(String faction) {
		factions.add(Objects.requireNonNull(faction, "faction"));
	}
	
	/**
	 * Checks whether a creature belongs to a faction.
	 * 
	 * @param faction	the faction id
	 * @return	{@code true} if the creature belongs to the faction, {@code false} otherwise
	 */
	public boolean isMember(String faction) {
		return factions.contains(faction);
	}
}
