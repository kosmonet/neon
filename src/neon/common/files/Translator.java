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

package neon.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A translator translates between a Java input/output stream and a specific
 * Java type.
 * 
 * @author mdriesen
 * @param <E>
 */
public interface Translator<E> {
	/**
	 * Translates an InputStream into another type.
	 * 
	 * @param input
	 * @return
	 * @throws IOException	if translating from the input stream fails
	 */
	public E translate(InputStream input) throws IOException;
	
	/**
	 * Translates a Java type to an OutputStream.
	 * 
	 * @param output
	 * @param out
	 * @throws IOException	if translating to the output stream fails
	 */
	public void translate(E out, OutputStream output) throws IOException;
}
