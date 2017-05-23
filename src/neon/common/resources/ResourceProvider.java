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

/**
 * Resources are obtained from a resource provider. Concrete implementations
 * may store their resources on disk, in memory or somewhere else. 
 * 
 * @author mdriesen
 *
 */
public interface ResourceProvider {
	/**
	 * Returns a resource from a specific namespace.
	 * 
	 * @param namespace
	 * @param id
	 * @return the requested resource
	 * @throws ResourceException 
	 */
	public <T extends Resource> T getResource(String namespace, String id) throws ResourceException;
}
