package balint.lenart.controllers.install;

import balint.lenart.Configuration;
import balint.lenart.model.helper.DatabaseConnectionProperties;
import balint.lenart.utils.DbUtil;
import balint.lenart.utils.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class InstallLayoutController {

    private static final Logger LOGGER = Logger.getLogger(InstallLayoutController.class);
    private Runnable saveCallback;

    @FXML private AnchorPane rootPane;

    // Postgres connection UI elements
    @FXML private TextField postgresHostField;
    @FXML private TextField postgresPortField;
    @FXML private TextField postgresDatabaseField;
    @FXML private TextField postgresSchemeField;
    @FXML private TextField postgresUserField;
    @FXML private PasswordField postgresPasswordField;

    // Mongo connection UI elements
    @FXML private TextField mongoHostField;
    @FXML private TextField mongoPortField;
    @FXML private TextField mongoDatabaseField;
    @FXML private CheckBox mongoUsePasswordField;
    @FXML private TextField mongoUsernameField;
    @FXML private PasswordField mongoPasswordField;

    @FXML
    private void initialize() {
        fillFieldsFromConfiguration();
    }

    private void fillFieldsFromConfiguration() {
        bindFields();
        fillPostgresFields();
        fillMongoFields();
    }

    private void bindFields() {
        mongoUsePasswordField.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean passwordFieldsEnabled = BooleanUtils.isTrue(newValue);
            mongoUsernameField.setDisable(!passwordFieldsEnabled);
            mongoPasswordField.setDisable(!passwordFieldsEnabled);
            if( passwordFieldsEnabled ) {
                mongoUsernameField.setText(null);
                mongoPasswordField.setText(null);
            }
        });
    }

    private void fillMongoFields() {
        mongoDatabaseField.setText( Configuration.get("mongo.connection.database") );
        mongoHostField.setText( Configuration.get("mongo.connection.host") );
        mongoPasswordField.setText( Configuration.get("mongo.connection.password") );
        mongoPortField.setText( Configuration.get("mongo.connection.port") );
        mongoUsePasswordField.setSelected( Configuration.getBoolean("mongo.connection.usepassword") );
        mongoUsernameField.setText( Configuration.get("mongo.connection.username") );
    }

    private void fillPostgresFields() {
        postgresDatabaseField.setText( Configuration.get("postgres.connection.database") );
        postgresHostField.setText( Configuration.get("postgres.connection.host") );
        postgresPasswordField.setText( Configuration.get("postgres.connection.password") );
        postgresPortField.setText( Configuration.get("postgres.connection.port") );
        postgresSchemeField.setText( Configuration.get("postgres.connection.schema") );
        postgresUserField.setText( Configuration.get("postgres.connection.username") );
    }

    @FXML
    private void handleTest() {
        if( testConnections() ) {
            NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Teszt",
                    "A kapcsolódás sikeres!", "Ezeket a beállításokat biztonságosan lehet használni!");
        } else {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Teszt",
                    "A paraméterek nem megfelelőek!", "Kérem nézze át újra a paramétereket és próbálja újra a csatlakozást!");
        }
    }

    private boolean testConnections() {
        try {
            checkFields();
            return DbUtil.checkConnection(getPostgresConnectionProperties(), getMongoConnectionProperties());
        } catch (IllegalArgumentException ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba",
                    "Az alábbi hibák léptek fel az ellenőrzés során:", ex.getMessage());
        }
        return false;
    }

    private DatabaseConnectionProperties getPostgresConnectionProperties() {
        return new DatabaseConnectionProperties(
                postgresHostField.getText(),
                Integer.valueOf(postgresPortField.getText()),
                postgresDatabaseField.getText(),
                postgresUserField.getText(),
                postgresPasswordField.getText()
        );
    }

    private DatabaseConnectionProperties getMongoConnectionProperties() {
        return new DatabaseConnectionProperties(
                mongoHostField.getText(),
                Integer.valueOf(mongoPortField.getText()),
                mongoDatabaseField.getText(),
                mongoUsernameField.getText(),
                mongoPasswordField.getText()
        );
    }

    private void checkFields() throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();

        // mongo fields
        if(StringUtils.isEmpty(mongoHostField.getText().trim())) {
            builder.append("A \"Hoszt\" mező kitöltése kötelező!\n");
        }
        if(StringUtils.isEmpty(mongoPortField.getText().trim())) {
            builder.append("A \"Port\" mező kitöltése kötelező!\n");
        } else {
            if(!StringUtils.isNumeric(mongoPortField.getText().trim())) {
                builder.append("A \"Port\" mező értéke csak szám lehet!\n");
            }
        }
        if(StringUtils.isEmpty(mongoDatabaseField.getText().trim())) {
            builder.append("Az \"Adatbázis\" mező kitöltése kötelező!\n");
        }
        if(mongoUsePasswordField.isSelected()) {
            if(StringUtils.isEmpty(mongoUsernameField.getText())) {
                builder.append("A \"Felhasználó\" mező kitöltése kötelező!\n");
            }

            if(StringUtils.isEmpty(mongoPasswordField.getText())) {
                builder.append("A \"Jelszó\" mező kitöltése kötelező!\n");
            }
        }

        // postgres fields
        if(StringUtils.isEmpty(postgresHostField.getText().trim())) {
            builder.append("A \"Hoszt\" mező kitöltése kötelező!\n");
        }
        if(StringUtils.isEmpty(postgresPortField.getText().trim())) {
            builder.append("A \"Port\" mező kitöltése közelező!\n");
        }
        if(!StringUtils.isNumeric(postgresPortField.getText().trim())) {
            builder.append("A \"Port\" mező csak számot tartalmazhaz!\n");
        }
        if(StringUtils.isEmpty(postgresDatabaseField.getText().trim())) {
            builder.append("Az \"Adatbázis\" mező kitöltése kötelező!\n");
        }
        if(StringUtils.isEmpty(postgresSchemeField.getText().trim())) {
            builder.append("A \"Séma\" mező kitöltése közelező!\n");
        }
        if(StringUtils.isEmpty(postgresUserField.getText().trim())) {
            builder.append("A \"Felhasználó\" mező kitöltése közelező!\n");
        }
        if(StringUtils.isEmpty(postgresPasswordField.getText().trim())) {
            builder.append("A \"Jelszó\" mező kitöltése közelező!\n");
        }

        if( !builder.toString().isEmpty() ) {
            throw new IllegalArgumentException(builder.toString());
        }
    }

    @FXML
    private void handleSave() {
        try {
            if( testConnections() ) {
                NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Teszt",
                        "A kapcsolódás sikeres!", "Ezeket a beállításokat biztonságosan lehet használni!");
            } else {
                NotificationUtil.showNotification(Alert.AlertType.ERROR, "Teszt",
                        "A paraméterek nem megfelelőek!", "Kérem nézze át újra a paramétereket és próbálja újra a csatlakozást!");
                return;     // TODO: 2016.10.04. isn't petty
            }
            commit();
            Configuration.saveToFile();
            saveCallback.run();
        } catch (Exception ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba",
                    "A mentés során váratlan hiba lépett fel", ex.getMessage());
            LOGGER.error("Nem sikerült a beállítások mentése ...", ex);
            ex.printStackTrace();
        }
    }

    private void commit() {
        Configuration.set("mongo.connection.host", mongoHostField.getText().trim());
        Configuration.set("mongo.connection.port", mongoPortField.getText().trim());
        Configuration.set("mongo.connection.database", mongoDatabaseField.getText().trim());
        Configuration.set("mongo.connection.usepassword", BooleanUtils.toStringTrueFalse(mongoUsePasswordField.isSelected()));
        Configuration.set("mongo.connection.password", mongoPasswordField.getText());
        Configuration.set("mongo.connection.username", mongoUsernameField.getText());
        Configuration.set("postgres.connection.host", postgresHostField.getText());
        Configuration.set("postgres.connection.port", postgresPortField.getText());
        Configuration.set("postgres.connection.database", postgresDatabaseField.getText());
        Configuration.set("postgres.connection.schema", postgresSchemeField.getText());
        Configuration.set("postgres.connection.username", postgresUserField.getText());
        Configuration.set("postgres.connection.password", postgresPasswordField.getText());
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }
}
