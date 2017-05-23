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

import com.google.common.collect.Multimap;

import neon.common.event.NeonEvent;

public class LoadEvent extends NeonEvent {	
	private final String id;
	private final Multimap<String, Card> cards;
	
	/**
	 * Creates a new {@code LoadEvent}.
	 * 
	 * @param id the id of the module that was loaded
	 */
	private LoadEvent(String id, Multimap<String, Card> cards) {
		this.id = id;
		this.cards = cards;
	}
	
	public Multimap<String, Card> getCards() {
		return cards;
	}
	
	public String getModuleID() {
		return id;
	}
	
	public static class Load extends LoadEvent {
		private final File file;

		public Load(File file, Multimap<String, Card> cards) {
			super(file.getName(), cards);
			this.file = file;
		}		
		
		public File getFile() {
			return file;
		}
	}
	
	public static class Create extends LoadEvent {
		private final Path path;
		
		public Create(Path path, Multimap<String, Card> cards) {
			super(path.toFile().getName(), cards);
			this.path = path;
		}
		
		public Path getPath() {
			return path;
		}
	}
}
