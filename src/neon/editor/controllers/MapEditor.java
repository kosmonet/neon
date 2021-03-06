/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

package neon.editor.controllers;

import java.awt.Rectangle;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import neon.common.entity.Entity;
import neon.common.graphics.RenderPane;
import neon.editor.Card;
import neon.editor.SaveEvent;
import neon.editor.SelectionEvent;
import neon.editor.resource.ICreature;
import neon.editor.resource.REntity;
import neon.editor.resource.RMap;
import neon.editor.ui.EditorRenderer;
import neon.common.resources.RCreature;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;

/**
 * <p>The map editor presents a pane on the main window to edit the currently
 * selected map. Three views are available:</p>
 * <ul>
 * 	<li>Info: edit general map information</li>
 * 	<li>Terrain: edit the terrain and add entities to the map</li>
 * 	<li>Height: edit the height map</li>
 * </ul>
 * 
 * @author mdriesen
 *
 */
public final class MapEditor {
	private static final ButtonType yes = new ButtonType("Yes", ButtonData.OK_DONE);
	private static final ButtonType no = new ButtonType("No", ButtonData.CANCEL_CLOSE);
	private static final Logger logger = Logger.getGlobal();
	
	private final Card card;
	private final GridPane grid = new GridPane();
	private final RenderPane<Entity> renderer;
	private final Pane pane = new Pane();
	private final ScrollPane scroller = new ScrollPane();
	private final EventBus bus;
	private final TextField nameField = new TextField();
	private final Spinner<Integer> widthSpinner, heightSpinner;
	private final short uid;
	private final String module;
	private final ResourceManager resources;
	private final ContextMenu menu = new ContextMenu();
	
	private boolean saved = true;
	private int scale = 20;
	private String terrain;
	private ImageCursor cursor;
	private Mode mode = Mode.EDIT;
	
	/**
	 * Initializes this map editor.
	 * 
	 * @param card
	 * @param resources
	 * @param bus
	 * @throws ResourceException	if the map resource can't be loaded
	 */
	public MapEditor(Card card, ResourceManager resources, EventBus bus) throws ResourceException {
		this.bus = bus;
		this.card = card;
		this.resources = resources;
		RMap map = card.getResource();
		uid = map.uid;
		module = map.module;
		
		grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(10, 10, 10, 10));
	    
	    Label name = new Label("Name");
	    grid.add(name, 0, 0);
	    grid.add(nameField, 1, 0);
	    nameField.setText(map.name);

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
	    
		pane.setPrefSize(scale*map.getWidth(), scale*map.getHeight());
		pane.setStyle("-fx-background-color: black;");
	    pane.setOnMouseClicked(this::mouseClicked);
		
		renderer = new RenderPane<Entity>(resources, new EditorRenderer());
		renderer.setMap(map);
	    renderer.setStyle("-fx-background-color: black;");
	    renderer.setOnMouseEntered(event -> mouseEntered());
	    renderer.setOnMouseExited(event -> mouseExited());
	    renderer.setOnDragOver(this::dragOver);
	    renderer.setOnDragDropped(this::dragDropped);
	    
	    // redraw when resizing or scrolling the map
	    renderer.widthProperty().addListener(value -> redraw());
	    renderer.heightProperty().addListener(value -> redraw());
	    scroller.widthProperty().addListener(value -> redraw());
	    scroller.heightProperty().addListener(value -> redraw());
	    scroller.vvalueProperty().addListener(value -> redraw());
	    scroller.hvalueProperty().addListener(value -> redraw());
		
	    showInfo();	// show the info pane by default
	}
	
	/**
	 * Shows the correct mouse pointer when dragging an entity over the map
	 * editor.
	 * 
	 * @param event
	 */
	private void dragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        
        event.consume();
	}
	
	/**
	 * Adds a new entity to the map when dragged from the resource tree.
	 * 
	 * @param event
	 */
	private void dragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasString()) {
        	addCreature(db.getString(), (int) Math.floor(event.getX()/scale), (int) Math.floor(event.getY()/scale));
        }

        event.setDropCompleted(true);
        event.consume();		
	}
	
	/**
	 * Adds a creature to the map at the given coordinates.
	 * 
	 * @param id
	 * @param x
	 * @param y
	 */
	private void addCreature(String id, int x, int y) {
		try {
			RCreature resource = resources.getResource("creatures", id);
			RMap map = card.getResource();
			ICreature creature = new ICreature(map.getFreeUID(), id, resource.glyph, resource.color);
			creature.shape.setPosition(x, y, 0);
			map.add(creature);
			redraw();
		} catch (ResourceException e) {
			logger.warning("could not add creature <" + id + "> to map");
		}
	}

	/**
	 * 
	 * @return the scrollpane used to display the map
	 */
	public ScrollPane getPane() {
		return scroller;
	}
	
	/**
	 * Redraws the map if the scrollpane was scrolled or resized. Due to a 
	 * problem with large drawings inside a scrollpane in JavaFX, drawing of 
	 * the map is a bit weird. Instead of stretching the render pane to the 
	 * full size of the map, an empty pane is created with the size of the map. 
	 * This empty pane is then added to the scrollpane. This way, the scrolling 
	 * behavior is correct. When the map has to be drawn, the visible part is 
	 * drawn on the actual render pane, which has the same size as the 
	 * scrollpane viewport. This render pane is then translated to the correct 
	 * position on the empty pane, so that the right bit of the map shows 
	 * through the viewport.
	 */
	private void redraw() {
		renderer.setPrefSize(scroller.getViewportBounds().getWidth(), scroller.getViewportBounds().getHeight());
		
        double contentWidth = pane.getLayoutBounds().getWidth();
        double viewportWidth = scroller.getViewportBounds().getWidth();
        double x = Math.max(0, contentWidth - viewportWidth) * scroller.getHvalue();

        double contentHeight = pane.getLayoutBounds().getHeight();
        double viewportHeight = scroller.getViewportBounds().getHeight();
        double y = Math.max(0,  contentHeight - viewportHeight) * scroller.getVvalue();
        
		renderer.draw((int) x/scale, (int) y/scale, scale);
		renderer.setTranslateX(x);
		renderer.setTranslateY(y);
	}
	
	/**
	 * Redraws the map if a resource (e.g. terrain) was changed that might 
	 * have an effect on the visuals.
	 * 
	 * @param event
	 */
	@Subscribe
	private void onResourceSave(SaveEvent.Resources event) {
		redraw();
	}
	
	/**
	 * Sets the mode the map editor is working in: painting terrain or editing
	 * entities.
	 * 
	 * @param mode
	 */
	void setMode(Mode mode) {
		this.mode = mode;
	}
	
	/**
	 * Checks for unsaved changes when the tab is closed.
	 */
	void close() {
		if (!saved) {
			Alert alert = new Alert(AlertType.CONFIRMATION, 
					"Save map before closing?", yes, no);
			alert.setTitle("Warning");
			alert.setHeaderText("Map contains unsaved changes.");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){	
				save();
			} 
		}

		// and don't forget to remove this tab from the bus
		bus.unregister(this);
	}
	
	/**
	 * Shows the map information pane.
	 */
	void showInfo() {
		scroller.setContent(grid);
	}
	
	/**
	 * Shows the map as it is seen in the game (more or less). The map can be 
	 * edited in this view.
	 */
	void showTerrain() {
		scroller.setContent(pane);
		pane.getChildren().clear();
		pane.getChildren().add(renderer);
		redraw();
	}
	
	/**
	 * Shows the height map.
	 */
	void showElevation() {
		scroller.setContent(new Label("showing " + card + " height"));
	}
	
	/**
	 * Saves the changes to a new map resource, overwriting the original.
	 */
	public void save() {
		card.setRedefined(card.isOriginal() ? true : false);
		String name = nameField.getText();
		name = name.isEmpty() ? card.toString() : name;
		widthSpinner.increment(0);	// trick to commit typed text when enter was not pressed
		heightSpinner.increment(0);
		RMap map = new RMap(card.toString(), name, widthSpinner.getValue(), heightSpinner.getValue(), uid, module);
		try {
			for (Entry<Rectangle, String> entry : card.<RMap>getResource().getTerrain().getElements().entrySet()) {
				Rectangle r = entry.getKey();
				map.getTerrain().insert(entry.getValue(), r.x, r.y, r.width, r.height);
			}
			for (Entry<Rectangle, Integer> entry : card.<RMap>getResource().getElevation().getElements().entrySet()) {
				Rectangle r = entry.getKey();
				map.getElevation().insert(entry.getValue(), r.x, r.y, r.width, r.height);
			}
			for (REntity entity : card.<RMap>getResource().getEntities()) {
				map.add(entity);
			}
		} catch (ResourceException e) {
			logger.severe("could not save map: " + e.getMessage());
		}

		renderer.setMap(map);
		bus.post(new SaveEvent.Resources(map));
		card.setChanged(true);
		saved = true;
	}
	
	/**
	 * Sets the terrain used for drawing in the terrain view.
	 * 
	 * @param event
	 */
	@Subscribe
	public void onTerrainSelect(SelectionEvent.Terrain event) {
		terrain = event.id;
	}
	
	/**
	 * Sets the cursor for the terrain view.
	 * 
	 * @param cursor
	 */
	void setCursor(ImageCursor cursor) {
		this.cursor = cursor;
	}
	
	/**
	 * Sets the correct cursor when the mouse enters the terrain view.
	 */
	private void mouseEntered() {
		if (terrain != null && cursor != null && mode == Mode.PAINT) {
			scroller.getScene().setCursor(cursor);			
		}
	}
	
	/**
	 * Resets the cursor to default when the mouse exits the terrain view.
	 */
	private void mouseExited() {
		scroller.getScene().setCursor(Cursor.DEFAULT);
	}
	
	/**
	 * Draws terrain or adds a resource when the mouse is clicked on the 
	 * terrain view.
	 * 
	 * @param me
	 */
	private void mouseClicked(MouseEvent me) {
		menu.hide();
		int x = (int) Math.floor(me.getX()/scale);
		int y = (int) Math.floor(me.getY()/scale);

		try {
			RMap map = card.getResource();
			
			if (mode == Mode.PAINT && terrain != null && me.getButton().equals(MouseButton.PRIMARY)) {
				int width = (int) Math.ceil(cursor.getImage().getWidth()/scale);
				map.getTerrain().insert(terrain, x - width/2, y - width/2, width, width);						
				redraw();
			} else if (mode == Mode.EDIT && me.getButton().equals(MouseButton.SECONDARY) && !map.getEntities(x, y).isEmpty()) {
				menu.getItems().clear();
				for (REntity entity : map.getEntities(x, y)) {
					MenuItem delete = new MenuItem("Delete " + entity.getID());
					delete.setOnAction(new DeleteHandler(map, entity));
					menu.getItems().add(delete);
				}
				menu.show(pane, me.getScreenX(), me.getScreenY());
			}
		} catch (ResourceException e) {
			logger.warning("could not load map <" + card.toString() + ">");
		}			
	}
	
	/**
	 * Handles the 'Delete entity' menu items in the context menu.
	 * 
	 * @author mdriesen
	 *
	 */
	private final class DeleteHandler implements EventHandler<ActionEvent> {
		private final REntity entity;
		private final RMap map;
		
		private DeleteHandler(RMap map, REntity entity) {
			this.map = map;
			this.entity = entity;
		}

		@Override
		public void handle(ActionEvent event) {
			map.removeEntity(entity);
			redraw();
		}
	}
	
	/**
	 * The map editor has two modes of operation:
	 * <ul>
	 * 	<li>painting terrain</li>
	 * 	<li>editing entities</li>
	 * </ul>
	 * 
	 * @author mdriesen
	 *
	 */
	public enum Mode {
		PAINT, EDIT;
	}
}
