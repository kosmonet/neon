/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018-2019 - Maarten Driesen
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

import java.util.Objects;

import neon.common.resources.Resource;

/**
 * A spell resource.
 * 
 * @author mdriesen
 *
 */
public final class RSpell extends Resource {
	public final Effect effect;
	public final Target target;
	public final int duration;
	public final int magnitude;
	public final String name;
	
	private final int hash;
	
	/**
	 * Initializes a new spell resource. The name, effect and target must not 
	 * be null.
	 * 
	 * @param id
	 * @param name
	 * @param effect
	 * @param target
	 * @param duration
	 * @param magnitude
	 */
	RSpell(String id, String name, Effect effect, Target target, int duration, int magnitude) {
		super(id, "spells");
		this.name = Objects.requireNonNull(name, "name");
		this.effect = Objects.requireNonNull(effect, "effect");
		this.target = Objects.requireNonNull(target, "target");
		this.duration = duration;
		this.magnitude = magnitude;
		hash = Objects.hash(duration, effect, magnitude, name, target);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + hash;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!super.equals(other)) {
			return false;
		} else if (other instanceof RSpell) {
			RSpell rs = (RSpell) other;
			return duration == rs.duration && effect == rs.effect && magnitude == rs.magnitude
					&& Objects.equals(name, rs.name) && target == rs.target;
		} else {
			return false;
		}
	}
}
