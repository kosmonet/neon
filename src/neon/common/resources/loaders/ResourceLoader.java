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

package neon.common.resources.loaders;

import org.jdom2.Element;

import neon.common.resources.Resource;

/**
 * Interface to load and save a resource from and to a JDOM XML element.
 * 
 * @author mdriesen
 *
 */
public interface ResourceLoader<T extends Resource> {
	/**
	 * Load a resource from a JDOM element.
	 * 
	 * @param root
	 * @return
	 */
	public T load(Element root);
	
	/**
	 * Save a resource to a JDOM element.
	 * 
	 * @param resource
	 * @return
	 */
	public Element save(T resource);
	
	/**
	 * Returns the extension of the file a resource is saved with.
	 * 
	 * @return
	 */
	public default String getExtension() {
		return ".xml";
	}
}
