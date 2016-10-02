package balint.lenart.controllers.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

/**
 * Created by lbalint on 2016.09.06..
 */
public class RootLayoutController {

    @FXML private Tab summaryTab;
    @FXML private Tab actionsTab;
    @FXML private Tab postgresTab;
    @FXML private Tab settingsTab;
    @FXML private Tab logTab;

    @FXML
    public void initialize() {
        actionsTab.setContent( getTabContent("main/migrationLayout.fxml") );
        postgresTab.setContent( getTabContent("main/postgresQueryLayout.fxml") );
        settingsTab.setContent( getTabContent("settings/settings.fxml") );
        logTab.setContent( getTabContent("main/logsViewer.fxml") );
    }

    private Node getTabContent(String fxmlFileName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/" + fxmlFileName));
        try {
            return loader.load();
        } catch (IOException ex) {
            System.out.println(ex);
            return new VBox(new Label("A panel betöltése nem sikerült ..."),
                    new Label(ExceptionUtils.getStackTrace(ex)));
        }
    }
}
