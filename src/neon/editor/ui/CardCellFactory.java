package neon.editor.ui;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import neon.editor.Card;

public class CardCellFactory implements Callback<TreeView<Card>, TreeCell<Card>> {
	@Override
	public TreeCell<Card> call(TreeView<Card> tree) {
	    return new CardTreeCell();
	}
	
	private class CardTreeCell extends TreeCell<Card> {
    	@Override
    	protected void updateItem(Card card, boolean empty) {
    		super.updateItem(card, empty);
    		if (!empty) {
    			setText(card.toString());
	    		if (card.isChanged()) {
	    			setStyle("-fx-font-style: italic");
	    			// TODO: parent in lichtgrijs, geherdefinieerde in bold
	    		} else {
	    			setStyle("-fx-font-style: normal");
	    		}
    		} else {
    			setText(null);	    			
    		}
    	}		
	}
}
