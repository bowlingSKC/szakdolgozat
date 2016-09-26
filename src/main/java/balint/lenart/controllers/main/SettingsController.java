package balint.lenart.controllers.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SettingsController {

    @FXML private Tab postgresTab;
    @FXML private Tab mongoTab;
    @FXML private Tab otherTab;

    @FXML
    private void initialize() {
        postgresTab.setContent( getTabContent("postgresSettings.fxml") );
        mongoTab.setContent( getTabContent("mongoSettings.fxml") );
        otherTab.setContent( getTabContent("migrationSettings.fxml") );
    }

    private Node getTabContent(String fxmlFileName) {
        FXMLLoader loader = new FXMLLoader( getClass().getResource("/layouts/settings/" + fxmlFileName) );
        try {
            return loader.load();
        } catch (Exception ex) {
            return new VBox(new Label("A panel betöltése nem sikerült ..."),
                    new Label(ExceptionUtils.getStackTrace(ex)));
        }
    }
}
