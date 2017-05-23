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

package neon.common.resources;

import java.util.ArrayList;
import java.util.Collection;

import neon.util.quadtree.RegionQuadTree;

public class RMap extends Resource {
	private final String name;
	private final RegionQuadTree<String> terrain;
	private final RegionQuadTree<Integer> elevation;
	private final ArrayList<Long> entities = new ArrayList<>();
	
	public RMap(String id, String name, int width, int height) {
		super(id, "map", "maps");
		this.name = name;
		terrain = new RegionQuadTree<>(width, height);
		elevation = new RegionQuadTree<>(width, height);
	}
	
	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return terrain.getWidth();
	}
	
	public int getHeight() {
		return terrain.getHeight();
	}
	
	public RegionQuadTree<String> getTerrain() {
		return terrain;
	}
	
	public RegionQuadTree<Integer> getElevation() {
		return elevation;
	}
	
	public Collection<Long> getEntities() {
		return entities;
	}
}
