package balint.lenart;

import balint.lenart.controllers.login.PasswordController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {

    private Stage primaryStage;

    public Main() throws SQLException {
        new Install().run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/start/password.fxml"));
        Pane rootLayout = loader.load();

        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);

        PasswordController controller = loader.getController();
        controller.setLoginCallback(this::loginCallback);

        primaryStage.setResizable(false);
        primaryStage.setTitle(Configuration.get("application.title"));
        primaryStage.show();
    }

    private void loginCallback() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/main/RootLayout.fxml"));
        try {
            Pane layout = loader.load();
            Scene scene = new Scene(layout);
            this.primaryStage.setScene(scene);
            this.primaryStage.centerOnScreen();
            this.primaryStage.setResizable(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Application.launch(args);
    }
}
