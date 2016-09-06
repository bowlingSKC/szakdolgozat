package balint.lenart.controllers.login;

import balint.lenart.Configuration;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import org.apache.commons.lang3.BooleanUtils;

public class PasswordController {

    private static final String PASSWORD = "pswd";  // TODO: 2016.08.25. move to settings

    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private Runnable loginCallback;

    @FXML
    private void initialize() {
        if(BooleanUtils.toBoolean(Configuration.get("application.login.autofillpassword"))) {
            passwordField.setText( PASSWORD );
        }
    }

    @FXML
    private void handleLogin() {
        loginButton.setDisable(true);
        if( PASSWORD.equals( passwordField.getText() ) ) {
            loginCallback.run();
        } else {
            Alert notification = new Alert(Alert.AlertType.ERROR);
            notification.setTitle("Hibás jelszó!");
            notification.setHeaderText("A bejelentkezés nem sikerült!");
            notification.setContentText("A megadott jelszó nem megfelelő!");
            notification.show();
        }
        loginButton.setDisable(false);
    }

    public void setLoginCallback(Runnable loginCallback) {
        this.loginCallback = loginCallback;
    }
}
