package balint.lenart.controllers.main;


import balint.lenart.controllers.helper.NamedEnumButtonCell;
import balint.lenart.log.Log;
import balint.lenart.services.LogService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LogViewerController {

    private final LogService logService = new LogService();

    @FXML private ComboBox<Log.LogType> typeSelector;
    @FXML private ListView<Log> logListView;
    @FXML private TextArea messageArea;

    @FXML
    private void initialize() {
        initTypeSelector();
        initListView();
    }

    private void initListView() {
        logListView.setCellFactory(param -> new ListCell<Log>() {
            @Override
            protected void updateItem(Log item, boolean empty) {
                super.updateItem(item, empty);
                if( item == null || empty ) {
                    setText("");
                } else {
                    setText(item.getFileName());
                }
            }
        });

        logListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadLogToMessageArea(newValue);
        });
    }

    private void loadLogToMessageArea(Log log) {
        messageArea.clear();
        messageArea.setText( log.getContent() != null ? log.getContent() : "Üres a kiválasztott log tartalma" );
    }

    private void initTypeSelector() {
        typeSelector.getItems().setAll( Log.LogType.values() );
        typeSelector.setButtonCell(new NamedEnumButtonCell<>());
        typeSelector.setCellFactory(param -> new NamedEnumButtonCell<>());
        typeSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadLogsToListView(newValue);
        });
    }

    private void loadLogsToListView(Log.LogType logType) {
        if(Log.LogType.APPLICATION.equals(logType)) {
            logListView.getItems().setAll( logService.getApplicationLogs() );
        } else if(Log.LogType.MIGRATION.equals(logType)) {
            logListView.getItems().setAll( logService.getMigrationLogs() );
        }
    }

}
