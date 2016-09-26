package balint.lenart.controllers.main.settings;

import balint.lenart.Configuration;
import balint.lenart.model.helper.PostgresConnectionProperties;
import balint.lenart.utils.DbUtil;
import balint.lenart.utils.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

public class PostgresSettingsController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField databaseField;
    @FXML private TextField schemeField;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;

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
            NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Beállítások",
                    "Sikeres mentés!", "Az adatok sikeresen mentve lettek!");
        } catch (Exception ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba", "A következő hibák léptek fel mentés közben:",
                    ex.getMessage());
        }
    }

    @FXML
    private void handleTestConnection() {
        try {
            checkFieldValidation();
            if(DbUtil.testPostgresConnection(createPropertiesFromFields())) {
                NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Kapcsolódás adatbázishoz",
                        "A teszt sikerült!", "Sikeresen lehetett kapcsolódni a megadott adatbázishoz.");
            } else {
                NotificationUtil.showNotification(Alert.AlertType.ERROR, "Kapcsolódás adatábizshoz",
                        "A teszt sikertelen!", "A megadott adatokkal nem lehet kapcsolódni egy adatbázishoz sem.");
            }
        } catch (Exception ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba", "A következő hibák léptek fel tesztelés közben:",
                    ex.getMessage());
        }
    }

    private PostgresConnectionProperties createPropertiesFromFields() {
        PostgresConnectionProperties properties = new PostgresConnectionProperties();
        properties.setDbName( databaseField.getText().trim() );
        properties.setHost( hostField.getText().trim() );
        properties.setPassword( passwordField.getText() );
        properties.setPort( Integer.parseInt(portField.getText()) );
        properties.setUserName( userField.getText() );
        return properties;
    }

    private void checkFieldValidation() throws Exception {
        StringBuilder builder = new StringBuilder();
        if(StringUtils.isEmpty(hostField.getText().trim())) {
            builder.append("A \"Hoszt\" mező kitöltése kötelező!\n");
        }
        if(StringUtils.isEmpty(portField.getText().trim())) {
            builder.append("A \"Port\" mező kitöltése közelező!\n");
        }
        if(!StringUtils.isNumeric(portField.getText().trim())) {
            builder.append("A \"Port\" mező csak számot tartalmazhaz!\n");
        }
        if(StringUtils.isEmpty(databaseField.getText().trim())) {
            builder.append("Az \"Adatbázis\" mező kitöltése kötelező!\n");
        }
        if(StringUtils.isEmpty(schemeField.getText().trim())) {
            builder.append("A \"Séma\" mező kitöltése közelező!\n");
        }
        if(StringUtils.isEmpty(userField.getText().trim())) {
            builder.append("A \"Felhasználó\" mező kitöltése közelező!\n");
        }
        if(StringUtils.isEmpty(passwordField.getText().trim())) {
            builder.append("A \"Jelszó\" mező kitöltése közelező!\n");
        }
        if( StringUtils.isNotEmpty(builder) ) {
            throw new Exception(builder.toString());
        }
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
