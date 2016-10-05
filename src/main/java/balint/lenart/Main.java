package balint.lenart;

import balint.lenart.controllers.install.InstallLayoutController;
import balint.lenart.controllers.login.PasswordController;
import balint.lenart.utils.DbUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
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
        if( DbUtil.checkConnection(Configuration.getDefaultMongoConnectionProperties(),
                Configuration.getDefaultPostgresConnectionProperties()) ) {
            loadMigrationUI();
        } else {
            loadInstallUI();
        }
    }

    private void loadInstallUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/installLayout.fxml"));
        try {
            Pane layout = loader.load();
            Scene scene = new Scene(layout);
            this.primaryStage.setScene(scene);
            this.primaryStage.centerOnScreen();
            this.primaryStage.setResizable(false);

            InstallLayoutController controller = loader.getController();
            controller.setSaveCallback(this::loadMigrationUI);
        } catch (Exception ex) {
            LOGGER.error("A felhasználói felület betöltése sikertelen", ex);
            ex.printStackTrace();
        }
    }

    private void loadMigrationUI() {
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
            LOGGER.error("A felhasználói felület betöltése sikertelen", ex);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Application.launch(args);
    }
}
