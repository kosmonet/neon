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
import neon.system.resources.MissingLoaderException;
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
	
	private final TreeView<String> tree;
	private final ResourceManager resources;
	private final EventBus bus;
	private final Window parent;
	
	CreatureHandler(TreeView<String> creatureTree, ResourceManager resources, EventBus bus) {
		tree = creatureTree;
		parent = tree.getScene().getWindow();
		this.resources = resources;
		this.bus = bus;
		
		ContextMenu creatureMenu = new ContextMenu();
		MenuItem addItem = new MenuItem("Add creature");
		addItem.setOnAction(event -> addCreature(event));
		creatureMenu.getItems().add(addItem);
		MenuItem removeItem = new MenuItem("Remove creature");
		removeItem.setOnAction(event -> removeCreature(event));
		creatureMenu.getItems().add(removeItem);
		creatureTree.setContextMenu(creatureMenu);
	}
	
	void loadCreatures() {
		TreeItem<String> root = new TreeItem<>();
		tree.setRoot(root);
		tree.setShowRoot(false);
		tree.setOnMouseClicked(event -> mouseClicked(event));
		resources.addLoader("creature", new CreatureLoader());

		for (String creature : resources.listResources("creatures")) {
			TreeItem<String> item = new TreeItem<>(creature);
			root.getChildren().add(item);
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
				TreeItem<String> item = new TreeItem<>(id);
				tree.getRoot().getChildren().add(item);
            	new CreatureEditor(creature, bus).show(parent);
			} catch (MissingLoaderException e) {
				logger.severe(e.getMessage());
			} catch (IOException e) {
				logger.severe("could not create creature " + id);
			}
		}
	}
	
	private void removeCreature(ActionEvent event) {
		// remove from the creature tree
		TreeItem<String> selected = tree.getSelectionModel().getSelectedItem();
		tree.getRoot().getChildren().remove(selected);
		// remove from the temp folder
		resources.removeResource("creatures", selected.getValue());
	}
	
	private void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
            TreeItem<String> item = tree.getSelectionModel().getSelectedItem();
            if (item != null) {
				try {
					RCreature creature = resources.getResource("creatures", item.getValue());
	            	new CreatureEditor(creature, bus).show(parent);
				} catch (ResourceException e) {
					logger.warning("could not load creature resource: " + item.getValue());
				}
            }
        }
	}		
}