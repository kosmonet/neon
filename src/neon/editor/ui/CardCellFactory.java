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

package neon.editor.ui;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import neon.editor.Card;

/**
 * Factory class for producing {@code TreeCell}s. 
 * 
 * @author mdriesen
 *
 */
public final class CardCellFactory implements Callback<TreeView<Card>, TreeCell<Card>> {
	@Override
	public TreeCell<Card> call(TreeView<Card> tree) {
	    return new CardTreeCell();
	}
	
	private final class CardTreeCell extends TreeCell<Card> {
    	@Override
    	protected void updateItem(Card card, boolean empty) {
    		super.updateItem(card, empty);
    		if (!empty) {
    			setText(card.toString());
    			
    			if(card.isRedefined()) {
    				setTextFill(Color.DARKRED);
    			} else if(card.isOriginal()) {
    				setTextFill(Color.GRAY);
    			} else {
    				setTextFill(Color.BLACK);
    			}
    			
	    		if (card.isChanged()) {
	    			setStyle("-fx-font-style: italic");
	    		} else {
	    			setStyle("-fx-font-style: normal");
	    		}
    		} else {
    			setText(null);	    			
    		}
    	}		
	}
}
