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

package neon.features.magic;

import neon.common.resources.Resource;

public class RSpell extends Resource {
	public final Effect effect;
	public final Target target;
	public final int duration;
	public final int magnitude;
	
	public RSpell(String id, Effect effect, Target target, int duration, int magnitude) {
		super(id, "spells");
		this.effect = effect;
		this.target = target;
		this.duration = duration;
		this.magnitude = magnitude;
	}
}
