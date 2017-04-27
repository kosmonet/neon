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

package neon.editor.map;

import javafx.event.Event;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import neon.editor.Card;

public class MapEditor {
	private final Card card;
	
	public MapEditor(Card card) {
		this.card = card;
	}
	
	public Pane getPane() {
		return new GridPane();
	}
	
	public void close(Event event) {
		System.out.println("closing " + card + "!");
	}
	
	public void showInfo() {
		System.out.println("showing " + card + " info");
	}
	
	public void showTerrain() {
		System.out.println("showing " + card + " terrain");
	}
	
	public void showHeight() {
		System.out.println("showing " + card + " height");
	}
	
	public void save() {
		System.out.println("saving " + card);
	}
}
