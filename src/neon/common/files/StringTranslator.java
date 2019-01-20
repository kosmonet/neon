/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.google.common.io.CharStreams;

/**
 * A simple translator for UTF-8 strings.
 * 
 * @author mdriesen
 *
 */
public class StringTranslator implements Translator<String> {
	@Override
	public String translate(InputStream input) throws IOException {
		try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			return CharStreams.toString(reader);
		}	
	}

	@Override
	public void translate(String out, OutputStream output) throws IOException {
		try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
			writer.write(out);
		}
	}
}
