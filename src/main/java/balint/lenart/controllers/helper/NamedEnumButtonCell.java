package balint.lenart.controllers.helper;

import balint.lenart.model.helper.NamedEnum;
import javafx.scene.control.ListCell;

public class NamedEnumButtonCell<T extends NamedEnum> extends ListCell<T> {
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if( item == null || empty ) {
            setText("");
            setStyle("");
        } else {
            setText( item.getName() );
        }
    }
}
