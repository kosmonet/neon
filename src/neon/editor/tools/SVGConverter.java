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

package neon.editor.tools;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import neon.editor.resource.RMap;

/**
 * A small tool to convert SVG images to a quadtree map file.
 * 
 * @author mdriesen
 *
 */
public class SVGConverter {
	private static Namespace ns = Namespace.getNamespace("svg", "http://www.w3.org/2000/svg");
	
	public static void main(String[] args) throws FileNotFoundException, JDOMException, IOException {
		System.out.println("loading svg file");
		Document doc = new Document();
		File file = new File("temp/neon.svg");
		doc = new SAXBuilder().build(new FileInputStream(file));
		
		Element svg = doc.getRootElement();
		int width = Integer.parseInt(svg.getAttributeValue("width"));
		int height = Integer.parseInt(svg.getAttributeValue("height"));
		
		System.out.println("creating map");
		RMap map = new RMap("neon", "neon", width, height, (short) 0, "neon");
		
		for (Element e : svg.getChildren()) {
			System.out.println(e.getName());
		}
		
		Element terrain = svg.getChild("g", ns);
		int dx = 0;
		int dy = 13700;
		
		for (Element rect : terrain.getChildren("rect", ns)) {
			int x = Integer.parseInt(rect.getAttributeValue("x")) + dx;
			int y = Integer.parseInt(rect.getAttributeValue("y")) + dy;
			int w = Integer.parseInt(rect.getAttributeValue("width"));
			int h = Integer.parseInt(rect.getAttributeValue("height"));
			
			map.getTerrain().insert(new Rectangle(x, y, w, h), "grass");
		}
		
		System.out.println("saving map");
		save(map);
	}
	
	private static void save(RMap map) throws IOException {
		Element root = new Element("map");
		root.setAttribute("id", map.id);
		root.setAttribute("name", map.name);
		root.setAttribute("uid", Short.toString(map.uid));
		root.setAttribute("module", map.module);
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(map.getSize()));
		size.setAttribute("height", Integer.toString(map.getSize()));
		root.addContent(size);
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		Map<Rectangle, String> leaves = map.getTerrain().getLeaves();
		for (Entry<Rectangle, String> entry : leaves.entrySet()) {
			if(entry.getValue() != null) {
				Element region = new Element("region");
				region.setAttribute("x", Integer.toString(entry.getKey().x));
				region.setAttribute("y", Integer.toString(entry.getKey().y));
				region.setAttribute("w", Integer.toString(entry.getKey().width));
				region.setAttribute("h", Integer.toString(entry.getKey().height));
				region.setAttribute("id", entry.getValue());
				terrain.addContent(region);
			}
		}
		
		Element height = new Element("height");
		root.addContent(height);
		
		Element entities = new Element("entities");
		root.addContent(entities);
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(new Document(root), new FileWriter("temp/map.xml"));
	}
}
