package balint.lenart;

import javafx.scene.control.Alert;

public class NotificationUtil {

    public static void showNotification(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

}
