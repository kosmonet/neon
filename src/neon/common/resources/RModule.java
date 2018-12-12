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

package neon.common.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A module resource.
 * 
 * @author mdriesen
 *
 */
public final class RModule extends Resource {
	/** The title that should be displayed on the start screen. */
	public final String title;
	/** The subtitle that should be displayed on the start screen. */
	public final String subtitle;
	/** The introductory text that should be shown. */
	public final String intro;
	/** The starting map. */
	public final String map;
	/** The starting x position. */
	public final int x;
	/** The starting y position. */
	public final int y;
	/** The starting money. */
	public final int money;

	private final Set<String> species;
	private final Set<String> parents;
	private final List<String> items;
	private final Set<String> spells;

	/**
	 * Initializes this RModule with a builder.
	 * 
	 * @param builder
	 */
	private RModule(Builder builder) {
		super(builder.id, "global");
		title = builder.title;
		subtitle = builder.subtitle;
		intro = builder.intro;
		map = builder.map;
		x = builder.x;
		y = builder.y;
		money = builder.money;

		species = ImmutableSet.copyOf(builder.creatures);
		parents = ImmutableSet.copyOf(builder.parents);
		spells = ImmutableSet.copyOf(builder.spells);
		items = ImmutableList.copyOf(builder.items);
	}
	
	/**
	 * Returns the items the player starts the game with.
	 * 
	 * @return	an unmodifiable {@code Collection} of item id's
	 */
	public Collection<String> getStartItems() {
		return items;
	}
	
	/**
	 * Returns a set of spells the player starts with.
	 * 
	 * @return	an unmodifiable set of spell id's
	 */
	public Set<String> getStartSpells() {
		return spells;
	}
	
	/**
	 * Returns a set of species the player can choose from during character
	 * creation.
	 * 
	 * @return	an unmodifiable set of creature id's
	 */
	public Set<String> getPlayableSpecies() {
		return species;
	}
	
	/**
	 * Returns a set of all the parent modules this module depends on.
	 * 
	 * @return	an unmodifiable set of module id's
	 */
	public Set<String> getParentModules() {
		return parents;
	}
	
	/**
	 * A builder for RModules.
	 * 
	 * @author mdriesen
	 *
	 */
	public static final class Builder {
		private String id;
		private String title = "";
		private String subtitle = "";
		private String intro = "";
		private String map = "";
		private int x = -1;
		private int y = -1;
		private int money = -1;
		private Set<String> creatures = new HashSet<>();
		private Set<String> spells = new HashSet<>();
		private Set<String> parents = new HashSet<>();
		private List<String> items = new ArrayList<>();
		
		public Builder(String id) {
			this.id = Objects.requireNonNull(id, "id");
		}
		
		/**
		 * Builds a new module resource.
		 * 
		 * @return
		 */
		public RModule build() {
			return new RModule(this);
		}
		
		public Builder setTitle(String title) {
			this.title = Objects.requireNonNull(title, "title");
			return this;
		}
		
		public Builder setSubtitle(String subtitle) {
			this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
			return this;
		}
		
		public Builder setIntro(String intro) {
			this.intro = Objects.requireNonNull(intro, "intro");
			return this;
		}
		
		public Builder setStartMap(String map) {
			this.map = Objects.requireNonNull(map, "map");
			return this;
		}
		
		public Builder setStartPosition(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}
		
		public Builder setStartMoney(int money) {
			this.money = money;
			return this;
		}
		
		public Builder addStartItem(String item) {
			items.add(Objects.requireNonNull(item, "item"));
			return this;
		}
		
		public Builder addStartSpell(String spell) {
			spells.add(Objects.requireNonNull(spell, "spell"));
			return this;
		}
		
		public Builder addPlayableSpecies(String species) {
			creatures.add(Objects.requireNonNull(species, "species"));
			return this;
		}
		
		public Builder addParentModule(String module) {
			parents.add(Objects.requireNonNull(module, "module id"));
			return this;
		}
	}
}
