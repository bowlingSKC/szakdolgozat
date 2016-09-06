package balint.lenart.controllers.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;

public class SettingsController {

    @FXML private Tab postgresTab;
    @FXML private Tab mongoTab;
    @FXML private Tab otherTab;

    @FXML
    private void initialize() {
        postgresTab.setContent( getTabContent("postgresSettings.fxml") );
    }

    private Node getTabContent(String fxmlFileName) {
        FXMLLoader loader = new FXMLLoader( getClass().getResource("/layouts/settings/" + fxmlFileName) );
        try {
            return loader.load();
        } catch (Exception ex) {
            return null;
        }
    }
}
