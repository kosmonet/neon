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

package neon.util.spatial;

import java.awt.Rectangle;
import java.util.Map;

public interface RegionSpatialIndex<T> {
	/**
	 * 
	 * @return	all contiguous regions in the index
	 */
	public Map<Rectangle, T> getElements();
	
	/**
	 * Inserts a value.
	 * 
	 * @param bounds
	 * @param value
	 */
	public void insert(Rectangle bounds, T value);
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return	the value at the given position
	 */
	public T get(int x, int y);
}
