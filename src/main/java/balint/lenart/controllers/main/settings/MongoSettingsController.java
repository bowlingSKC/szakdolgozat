package balint.lenart.controllers.main.settings;

import balint.lenart.Configuration;
import balint.lenart.utils.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class MongoSettingsController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField databaseField;
    @FXML private CheckBox usePasswordField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void initialize() {
        bindUsePasswordField();
        fillFieldFromSettings();
    }

    private void bindUsePasswordField() {
        usePasswordField.selectedProperty().addListener((observable, oldValue, newValue) -> {
            usernameField.setDisable(!newValue);
            passwordField.setDisable(!newValue);

            if( !newValue ) {
                usernameField.setText(null);
                passwordField.setText(null);
            }
        });
    }

    private void fillFieldFromSettings() {
        hostField.setText(Configuration.get("mongo.connection.host"));
        portField.setText(Configuration.get("mongo.connection.port"));
        databaseField.setText(Configuration.get("mongo.connection.database"));
        usePasswordField.setSelected(BooleanUtils.toBoolean(Configuration.get("mongo.connection.usepassword")));
        usernameField.setText(Configuration.get("mongo.connection.username"));
        passwordField.setText(Configuration.get("mongo.connection.password"));
    }

    @FXML
    private void handleSave() {
        try {
            checkFields();
            commit();
            Configuration.saveToFile();
            NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Beállítások",
                    "Sikeres mentés!", "Az adatok sikeresen mentve lettek!");
        } catch (Exception ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba", "A következő hibák léptek fel mentés közben:",
                    ex.getMessage());
        }
    }

    private void checkFields() throws Exception {
        StringBuilder builder = new StringBuilder();

        if(StringUtils.isEmpty(hostField.getText().trim())) {
            builder.append("A \"Hoszt\" mező kitöltése kötelező!\n");
        }

        if(StringUtils.isEmpty(portField.getText().trim())) {
            builder.append("A \"Port\" mező kitöltése kötelező!\n");
        } else {
            if(!StringUtils.isNumeric(portField.getText().trim())) {
                builder.append("A \"Port\" mező értéke csak szám lehet!\n");
            }
        }

        if(StringUtils.isEmpty(databaseField.getText().trim())) {
            builder.append("Az \"Adatbázis\" mező kitöltése kötelező!\n");
        }

        if(usePasswordField.isSelected()) {
            if(StringUtils.isEmpty(usernameField.getText().trim())) {
                builder.append("A \"Felhasználó\" mező kitöltése kötelező!\n");
            }

            if(StringUtils.isEmpty(passwordField.getText().trim())) {
                builder.append("A \"Jelszó\" mező kitöltése kötelező!\n");
            }
        }

        if( !builder.toString().isEmpty() ) {
            throw new Exception(builder.toString());
        }
    }

    private void commit() {
        Configuration.set("mongo.connection.host", hostField.getText().trim());
        Configuration.set("mongo.connection.port", portField.getText().trim());
        Configuration.set("mongo.connection.database", databaseField.getText().trim());
        Configuration.set("mongo.connection.usepassword", BooleanUtils.toStringTrueFalse(usePasswordField.isSelected()));
        Configuration.set("mongo.connection.password", passwordField.getText());
        Configuration.set("mongo.connection.username", usernameField.getText());
    }

    @FXML
    private void handleTestConnection() {

    }

}
