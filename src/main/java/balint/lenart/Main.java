package balint.lenart;

import balint.lenart.controllers.login.PasswordController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;

    public Main() {
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
        primaryStage.setTitle(Configuration.Constants.WINDOW_TITLE);
        primaryStage.show();
    }

    private void loginCallback() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/main/RootLayout.fxml"));
        try {
            Pane layout = loader.load();
            Scene scene = new Scene(layout);
            this.primaryStage.setScene(scene);
            this.primaryStage.setMinWidth(Configuration.Constants.WINDOW_MIN_WIDTH);
            this.primaryStage.setMinHeight(Configuration.Constants.WINDOW_MIN_HEIGHT);
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
