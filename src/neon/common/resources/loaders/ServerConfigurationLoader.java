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

import java.util.LinkedHashSet;

import org.jdom2.Element;

import neon.common.resources.CServer;

/**
 * A resource loader for server configuration files.
 * 
 * @author mdriesen
 *
 */
public class ServerConfigurationLoader implements ResourceLoader<CServer> {
	@Override
	public CServer load(Element root) {
		// LinkedHashSet to preserve module load order and to prevent doubles
		LinkedHashSet<String> modules = new LinkedHashSet<String>();
		for (Element module : root.getChild("modules").getChildren()) {
			modules.add(module.getText());
		}
		
		String level = root.getChildText("log").toUpperCase();
		CServer cs = new CServer(modules, level);

		// extra step in case this was a saved configuration file
		if(root.getName().equals("server")) {
			for (Element module : root.getChild("modules").getChildren()) {
				cs.setModuleUID(module.getText(), Short.parseShort(module.getAttributeValue("uid")));
			}
		}

		return cs; 
	}

	@Override
	public Element save(CServer server) {
		Element root = new Element("server");

		Element modules = new Element("modules");
		for(String id : server.getModules()) {
			Element module = new Element("module");
			module.setText(id);
			module.setAttribute("uid", Short.toString(server.getModuleUID(id)));
			modules.addContent(module);
		}
		root.addContent(modules);
		
		Element log = new Element("log");
		log.setText(server.getLogLevel());
		root.addContent(log);
		
		return root;
	}		
}
