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

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import neon.editor.dialogs.CreatureEditor;
import neon.system.resources.CreatureLoader;
import neon.system.resources.RCreature;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;

/**
 * Class to handle all creature-related matters.
 * 
 * @author mdriesen
 *
 */
public class CreatureHandler {
	private final static Logger logger = Logger.getGlobal();
	
	private final TreeView<Card> tree;
	private final ResourceManager resources;
	private final EventBus bus;
	private Window parent;
	
	CreatureHandler(TreeView<Card> creatureTree, ResourceManager resources, EventBus bus) {
		tree = creatureTree;
		this.resources = resources;
		this.bus = bus;
		
		ContextMenu creatureMenu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add creature");
		addItem.setOnAction(event -> addCreature(event));
		creatureMenu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove creature");
		removeItem.setOnAction(event -> removeCreature(event));
		creatureMenu.getItems().add(removeItem);
		tree.setContextMenu(creatureMenu);
		
		tree.setCellFactory(new CardCellFactory());
	}
	
	/**
	 * Populates the creature tree with creatures.
	 */
	void loadCreatures() {
		parent = tree.getScene().getWindow();

		TreeItem<Card> root = new TreeItem<>();
		tree.setRoot(root);
		tree.setShowRoot(false);
		tree.setOnMouseClicked(event -> mouseClicked(event));
		resources.addLoader("creature", new CreatureLoader());

		for (String creature : resources.listResources("creatures")) {
			TreeItem<Card> item = new TreeItem<>(new Card("creatures", creature, resources));
			root.getChildren().add(item);
		}
	}
	
	@Subscribe
	private void save(SaveEvent event) {
		switch(event.toString()) {
		case "creatures":
			// tree cells don't automatically refresh if content is changed
			tree.refresh();
			break;
		case "module":
			// reset the changed status for all index cards
			tree.getRoot().getChildren().forEach(item -> item.getValue().setChanged(false));
			tree.refresh();
			break;
		}
	}
	
	private void addCreature(ActionEvent event) {
		// ask for a new creature id
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New Creature");
		dialog.setHeaderText("Give an id for the new creature.");
		dialog.setContentText("Creature id:");
		Optional<String> result = dialog.showAndWait();

		// check if id is valid
		if (result.isPresent() && !result.get().isEmpty()) {
			String id = result.get();
			if (resources.hasResource("creatures", id)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Creature conflict");
				alert.setContentText("The creature id already exists, use another id.");
				alert.showAndWait();
				return;
			}

			// create the creature
			RCreature creature = new RCreature(id, id);

			try {
				resources.addResource("creatures", creature);
				TreeItem<Card> item = new TreeItem<>(new Card("creatures", id, resources));
				tree.getRoot().getChildren().add(item);
            	new CreatureEditor(item.getValue(), bus).show(parent);
			} catch (IOException e) {
				logger.severe("could not create creature " + id);
			} catch (ResourceException e) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	private void removeCreature(ActionEvent event) {
		// remove from the creature tree
		TreeItem<Card> selected = tree.getSelectionModel().getSelectedItem();
		tree.getRoot().getChildren().remove(selected);
		// remove from the temp folder
		resources.removeResource("creatures", selected.getValue().toString());
	}
	
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<Card> item = tree.getSelectionModel().getSelectedItem();
            if (item != null) {
	            try {
					new CreatureEditor(item.getValue(), bus).show(parent);
				} catch (ResourceException e) {
					logger.severe(e.getMessage());
				}
            }
        }
	}	
}