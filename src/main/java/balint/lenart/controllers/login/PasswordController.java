package balint.lenart.controllers.login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class PasswordController {

    private static final String PASSWORD = "pswd";  // TODO: 2016.08.25. move to settings

    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin() {
        loginButton.setDisable(true);
        if( PASSWORD.equals( passwordField.getText() ) ) {

        } else {
            
        }
        loginButton.setDisable(false);
    }

}
