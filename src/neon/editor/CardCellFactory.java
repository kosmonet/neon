package neon.editor;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class CardCellFactory implements Callback<TreeView<Card>, TreeCell<Card>> {
	@Override
	public TreeCell<Card> call(TreeView<Card> tree) {
	    return new CardTreeCell();
	}
	
	private class CardTreeCell extends TreeCell<Card> {
    	@Override
    	protected void updateItem(Card entry, boolean empty) {
    		super.updateItem(entry, empty);
    		if (!empty) {
    			setText(entry.toString());
	    		if (entry.isChanged()) {
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
