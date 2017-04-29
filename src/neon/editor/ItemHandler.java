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

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import neon.editor.ui.CardCellFactory;

public class ItemHandler {
	@FXML private TreeView<Card> itemTree;
	
	@FXML public void initialize() {		
		itemTree.setCellFactory(new CardCellFactory());
	}
	
	private void loadResources() {
		TreeItem<Card> root = new TreeItem<>();
		itemTree.setShowRoot(false);
		itemTree.setRoot(root);
		itemTree.setOnMouseClicked(event -> mouseClicked(event));
		
		TreeItem<Card> items = new TreeItem<>(new Card.Type("Items"));
		root.getChildren().add(items);
		TreeItem<Card> doors = new TreeItem<>(new Card.Type("Doors"));
		root.getChildren().add(doors);
		TreeItem<Card> containers = new TreeItem<>(new Card.Type("Containers"));
		root.getChildren().add(containers);

//		for (String creature : resources.listResources("creatures")) {
//			TreeItem<Card> item = new TreeItem<>(new Card("creatures", creature, resources));
//			root.getChildren().add(item);
//		}
	}
	
	/**
	 * Signals to this handler that a module was loaded.
	 * 
	 * @param event
	 */
	@Subscribe
	private void load(LoadEvent event) {
		// module is loading on this tick, load creatures on the next tick
		Platform.runLater(() -> loadResources());
	}
	
	/**
	 * Opens an item editor when an item in the tree was double clicked.
	 * 
	 * @param event
	 */
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<Card> item = itemTree.getSelectionModel().getSelectedItem();
            // don't react when clicked on the item type headings
            if (item != null && !(item.getValue() instanceof Card.Type)) {
//	            try {
//					new CreatureEditor(item.getValue(), bus).show(parent);
//				} catch (ResourceException e) {
//					logger.severe(e.getMessage());
//				}
            }
        }
	}	
}
