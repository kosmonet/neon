/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2019 - Maarten Driesen
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

package neon.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * The XMLTranslator uses JDOM to translate between Java input/output streams
 * and JDOM Documents.
 * 
 * @author mdriesen
 *
 */
public final class XMLTranslator implements Translator<Document> {	
	private static final XMLOutputter OUTPUTTER = new XMLOutputter(Format.getPrettyFormat());

	@Override
	public Document translate(InputStream input) throws IOException {
		try {
			return new SAXBuilder().build(input);
		} catch (JDOMException e) {
			throw new IOException(e);
		} 
	}
	
	@Override
	public void translate(Document document, OutputStream output) throws IOException {
		OUTPUTTER.output(document, output);
	}
}
