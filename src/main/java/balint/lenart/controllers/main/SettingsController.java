package balint.lenart.controllers.main;

import balint.lenart.controllers.RefreshTabController;
import com.google.common.collect.Maps;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

public class SettingsController {

    private Map<Tab, RefreshTabController> controllers = Maps.newHashMap();

    @FXML private TabPane settingsTabpane;
    @FXML private Tab postgresTab;
    @FXML private Tab mongoTab;
    @FXML private Tab migrationTab;

    @FXML
    private void initialize() {
        setTabContent(postgresTab, "postgresSettings.fxml");
        setTabContent(mongoTab, "mongoSettings.fxml");
        setTabContent(migrationTab, "migrationSettings.fxml");

        settingsTabpane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            controllers.get(newValue).refreshTab();
        });
    }

    private void setTabContent(Tab tab, String fxmlFileName) {
        FXMLLoader loader = new FXMLLoader( getClass().getResource("/layouts/settings/" + fxmlFileName) );
        try {
            Node content = loader.load();
            RefreshTabController controller = loader.getController();

            tab.setContent( content );
            controllers.put( tab, controller );
        } catch (Exception ex) {
            tab.setContent(new VBox(new Label("A panel betöltése nem sikerült ..."),
                    new Label(ExceptionUtils.getStackTrace(ex))));
        }
    }
}
