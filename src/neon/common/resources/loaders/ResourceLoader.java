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

package neon.common.resources.loaders;

import java.io.IOException;
import java.util.Set;

import org.jdom2.DataConversionException;

import neon.common.resources.Resource;

/**
 * Interface to load and save a resource.
 * 
 * @author mdriesen
 *
 */
public interface ResourceLoader {
	/**
	 * Load a resource.
	 * 
	 * @param id	the id of a resource
	 * @return
	 * @throws IOException	if the resource can't be loaded
	 * @throws DataConversionException	if the resource contains the wrong type of data
	 */
	public Resource load(String id) throws IOException, DataConversionException;
	
	/**
	 * Saves a resource.
	 * 
	 * @param resource
	 * @throws IOException	if the resource can't be saved
	 */
	public void save(Resource resource) throws IOException;
	
	/**
	 * Lists all the resources in the namespace of this loader.
	 * 
	 * @return
	 */
	public Set<String> listResources();
	
	/**
	 * Removes a resource in the namespace of this loader.
	 * 
	 * @param id
	 * @throws IOException	if the resource can't be removed
	 */
	public void removeResource(String id) throws IOException;
	
	/**
	 * Returns the namespace this loader loads from.
	 * 
	 * @return
	 */
	public String getNamespace();
}
