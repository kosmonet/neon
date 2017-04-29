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

import com.google.common.eventbus.EventBus;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import neon.editor.Card;
import neon.editor.SaveEvent;
import neon.system.resources.RMap;
import neon.system.resources.ResourceException;

/**
 * The map editor presents a pane on the main window to edit the currently
 * selected map. Three views are available:
 * <ul>
 * 	<li>Info: edit general map information</li>
 * 	<li>Terrain: edit the terrain and add entities to the map</li>
 * 	<li>Height: edit the height map</li>
 * </ul>
 * 
 * @author mdriesen
 *
 */
public class MapEditor {
	private final Card card;
	private final GridPane grid = new GridPane();
	private final AnchorPane pane = new AnchorPane();
	private final EventBus bus;
	private final TextField nameField = new TextField();
	private final Spinner<Integer> widthSpinner, heightSpinner;
	
	public MapEditor(Card card, EventBus bus) throws ResourceException {
		this.bus = bus;
		this.card = card;
		RMap map = card.getResource();
		
		grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(10, 10, 10, 10));
	    
	    Label name = new Label("Name");
	    grid.add(name, 0, 0);
	    grid.add(nameField, 1, 0);
	    nameField.setText(map.getName());

	    Label width = new Label("Width");
	    grid.add(width, 0, 1);
	    widthSpinner = new Spinner<>(1, Integer.MAX_VALUE, map.getWidth());
	    widthSpinner.setEditable(true);
	    grid.add(widthSpinner, 1, 1);

	    Label height = new Label("Height");
	    grid.add(height, 0, 2);
	    heightSpinner = new Spinner<>(1, Integer.MAX_VALUE, map.getHeight());
	    heightSpinner.setEditable(true);
	    grid.add(heightSpinner, 1, 2);
	    
	    pane.getChildren().add(grid);
	}
	
	public Pane getPane() {
		return pane;
	}
	
	public void close(Event event) {
		System.out.println("closing " + card + "!");
	}
	
	public void showInfo() {
		pane.getChildren().clear();
		pane.getChildren().add(grid);
	}
	
	public void showTerrain() {
		System.out.println("showing " + card + " terrain");
	}
	
	public void showHeight() {
		System.out.println("showing " + card + " height");
	}
	
	/**
	 * Saves the changes to a new map resource, overwriting the original.
	 */
	public void save() {
		String name = nameField.getText();
		name = name.isEmpty() ? card.toString() : name;
		RMap map = new RMap(card.toString(), name);
		widthSpinner.increment(0);	// trick to commit typed text when enter was not pressed
		heightSpinner.increment(0);
		map.setSize(widthSpinner.getValue(), heightSpinner.getValue());
		
		bus.post(new SaveEvent.Resources("maps", map));
		card.setChanged(true);
	}
}
