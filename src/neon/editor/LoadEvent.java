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

package neon.editor;

import java.io.File;
import java.nio.file.Path;

import neon.system.event.NeonEvent;

public class LoadEvent extends NeonEvent {
	
	public LoadEvent(String message) {
		super(message);
	}
	
	public static class Load extends LoadEvent {
		private final File file;

		public Load(File file) {
			super(file.getName());
			this.file = file;
		}		
		
		public File getFile() {
			return file;
		}
	}
	
	public static class Create extends LoadEvent {
		private final Path path;
		
		public Create(Path path) {
			super(path.getFileName().toString());
			this.path = path;
		}
		
		public Path getPath() {
			return path;
		}
	}
}
