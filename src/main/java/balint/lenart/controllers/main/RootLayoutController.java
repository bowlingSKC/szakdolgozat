package balint.lenart.controllers.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;

import java.io.IOException;

/**
 * Created by lbalint on 2016.09.06..
 */
public class RootLayoutController {

    @FXML private Tab summaryTab;
    @FXML private Tab actionsTab;
    @FXML private Tab postgresTab;
    @FXML private Tab mongoTab;
    @FXML private Tab settingsTab;

    @FXML
    public void initialize() {
        settingsTab.setContent( getTabContent("settings/settings.fxml") );
    }

    private Node getTabContent(String fxmlFileName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/" + fxmlFileName));
        try {
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
