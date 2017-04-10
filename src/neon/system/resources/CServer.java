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

package neon.system.resources;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration information for the server. This resource should be created 
 * by the server when a new game is started, it cannot initially be loaded 
 * by the resource manager.
 * 
 * @author mdriesen
 *
 */
public class CServer extends Resource {
	private final Set<String> modules = new LinkedHashSet<>();

	public CServer() {
		super("server", "config");
	}
	
	public void setModules(Collection<String> modules) {
		this.modules.clear();
		this.modules.addAll(modules);
	}
	
	/**
	 * @return the list of modules to be loaded
	 */
	public String[] getModules() {
		String[] list = new String[modules.size()];
		return modules.toArray(list);
	}
}
