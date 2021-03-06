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

package neon.common.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A log formatter that only outputs the message, source and the priority 
 * level. No timestamp or other information is used.
 * 
 * @author mdriesen
 *
 */
public final class NeonLogFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		String source = record.getSourceClassName();
		String type = source.substring(source.lastIndexOf('.') + 1);
		String method = record.getSourceMethodName();
		String format = "%-7.7s %-36.36s %s";

		return String.format(format, record.getLevel(), "- " + type + "::" + method, "- " + record.getMessage() + "\r\n");	
//		return record.getLevel() + " - " + record.getMessage() + "\r\n";
	}
}
