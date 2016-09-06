package balint.lenart.controllers.main.settings;

import balint.lenart.Configuration;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PostgresSettingsController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField databaseField;
    @FXML private TextField schemeField;
    @FXML private TextField userField;
    @FXML private TextField passwordField;

    @FXML
    private void initialize() {
        hostField.setText(Configuration.get("postgres.connection.host"));
        portField.setText(Configuration.get("postgres.connection.port"));
        databaseField.setText(Configuration.get("postgres.connection.database"));
        schemeField.setText(Configuration.get("postgres.connection.schema"));
        userField.setText(Configuration.get("postgres.connection.username"));
        passwordField.setText(Configuration.get("postgres.connection.password"));
    }

    @FXML
    private void handleSave() {
        try {
            checkFieldValidation();
            commit();
            Configuration.saveToFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkFieldValidation() throws Exception {

    }

    private void commit() {
        Configuration.set("postgres.connection.host", hostField.getText());
        Configuration.set("postgres.connection.port", portField.getText());
        Configuration.set("postgres.connection.database", databaseField.getText());
        Configuration.set("postgres.connection.schema", schemeField.getText());
        Configuration.set("postgres.connection.username", userField.getText());
        Configuration.set("postgres.connection.password", passwordField.getText());
    }

}
