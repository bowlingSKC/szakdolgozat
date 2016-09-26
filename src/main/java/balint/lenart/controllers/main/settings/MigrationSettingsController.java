package balint.lenart.controllers.main.settings;

import balint.lenart.MigrationSettingsLevel;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

public class MigrationSettingsController {

    @FXML private ComboBox<MigrationSettingsLevel> levelSelector;

    @FXML
    private void initialize() {
        initLevelSelector();
        initQuestionTooltips();
    }

    private void initQuestionTooltips() {

    }

    private void initLevelSelector() {
        levelSelector.getItems().addAll( MigrationSettingsLevel.values() );
        levelSelector.setCellFactory(param -> new ListCell<MigrationSettingsLevel>() {
            @Override
            protected void updateItem(MigrationSettingsLevel item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText("");
                    setStyle("");
                } else {
                    setText( item.getName() );
                }
            }
        });
        levelSelector.setButtonCell(new ListCell<MigrationSettingsLevel>() {
            @Override
            protected void updateItem(MigrationSettingsLevel item, boolean empty) {
                super.updateItem(item, empty);

                if( item == null || empty ) {
                    setText("");
                    setStyle("");
                } else {
                    setText( item.getName() );
                }
            }
        });
    }


}
