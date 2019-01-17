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

package neon.common.resources;

import java.util.Objects;

/**
 * A resource that represents an html file.
 * 
 * @author mdriesen
 *
 */
public class RText extends Resource {
	/** The contents of the html file. */
	public final String text;
	
	/**
	 * The text must not be null.
	 * 
	 * @param id
	 * @param text
	 */
	public RText(String id, String text) {
		super(id, "texts");
		this.text = Objects.requireNonNull(text, "text");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + Objects.hash(text);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!super.equals(other)) {
			return false;
		} else if (other instanceof RText) {
			RText rt = (RText) other;
			return Objects.equals(text, rt.text);
		} else {
			return false;
		}
	}
}
