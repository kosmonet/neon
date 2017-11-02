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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * A tool to convert SVG images to a quadtree map file.
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
		
		Element terrain = svg.getChild("g", ns);		

		System.out.println("saving map");
		save(terrain, width, height);
		System.out.println("finished");
	}
	
	private static void save(Element shapes, int width, int height) throws IOException {
		Element root = new Element("map");
		root.setAttribute("id", "aneirin");
		root.setAttribute("name", "Aneirin");
		root.setAttribute("uid", "1");
		root.setAttribute("module", "aneirin");
		
		Element size = new Element("size");
		size.setAttribute("width", Integer.toString(width));
		size.setAttribute("height", Integer.toString(height));
		root.addContent(size);
		
		int dx = 0;
		int dy = 13700;
		
		Element terrain = new Element("terrain");
		root.addContent(terrain);
		
		Element base = new Element("region");
		base.setAttribute("x", "0");
		base.setAttribute("y", "0");
		base.setAttribute("w", Integer.toString(width));
		base.setAttribute("h", Integer.toString(height));
		base.setAttribute("id", "sea");
		terrain.addContent(base);
		
		for (Element rect : shapes.getChildren("rect", ns)) {
			int x = Integer.parseInt(rect.getAttributeValue("x")) + dx;
			int y = Integer.parseInt(rect.getAttributeValue("y")) + dy;
			int w = Integer.parseInt(rect.getAttributeValue("width"));
			int h = Integer.parseInt(rect.getAttributeValue("height"));
			
			String id = "grass";
			String style = rect.getAttributeValue("style");
			
			if (style.contains("#ffff00")) {
				id = "sand";
			} else if (style.contains("#ff0000")) {
				id = "mud";
			} else if (style.contains("#550000")) {
				id = "rock";
			} else if (style.contains("#22cc51")) {
				id = "moss";
			} else if (style.contains("#008080")) {
				id = "marsh";
			} else if (style.contains("#bcff00")) {
				id = "meadow";
			}

			Element region = new Element("region");
			region.setAttribute("x", Integer.toString(x));
			region.setAttribute("y", Integer.toString(y));
			region.setAttribute("w", Integer.toString(w));
			region.setAttribute("h", Integer.toString(h));
			region.setAttribute("id", id);
			terrain.addContent(region);
		}
		
		Element elevation = new Element("elevation");
		root.addContent(elevation);
		
		Element entities = new Element("entities");
		root.addContent(entities);
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(new Document(root), new FileWriter("temp/map.xml"));
	}
}
