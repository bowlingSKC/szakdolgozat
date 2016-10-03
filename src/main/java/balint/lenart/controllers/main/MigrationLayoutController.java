package balint.lenart.controllers.main;

import balint.lenart.Configuration;
import balint.lenart.model.helper.MigrationElement;
import balint.lenart.model.helper.NamedEnum;
import balint.lenart.services.Migrator;
import balint.lenart.utils.DateUtils;
import balint.lenart.utils.FXUtils;
import balint.lenart.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

import java.util.Date;

public class MigrationLayoutController {

    private static final Logger LOGGER = Logger.getLogger(MigrationLayoutController.class);
    private static final Migrator migrator = Migrator.getInstance();

    @FXML private HBox statBox;
    @FXML private Label sumOfMigratedEntityLabel;
    @FXML private Label sumOfFailedEntityLabel;
    @FXML private Label sumOfAllEntityLabel;
    @FXML private Button startButton;
    @FXML private Button cancelButton;
    @FXML private ProgressBar migrationProgressBar;
    @FXML private TableView<MigrationElement> migrationTable;
    @FXML private TableColumn<MigrationElement, Date> timeColumn;
    @FXML private TableColumn<MigrationElement, NamedEnum> entityColumn;
    @FXML private TableColumn<MigrationElement, Boolean> successColumn;
    @FXML private TextArea migrationOutput;

    @FXML
    private void initialize() {
        initTableView();
        initStatLabels();
        initProgressBar();
        bindMigratorProperties();
        setDefaultState();
    }

    private void initProgressBar() {
        migrationProgressBar.setTooltip(new Tooltip(Configuration.Constants.PBAR_TOOLTIP));
    }

    private void initStatLabels() {
        sumOfAllEntityLabel.setTooltip(new Tooltip(Configuration.Constants.ALL_MIGRATION_LABEL));
        sumOfFailedEntityLabel.setTooltip(new Tooltip(Configuration.Constants.FAILED_MIGRATION_LABEL));
        sumOfMigratedEntityLabel.setTooltip(new Tooltip(Configuration.Constants.SUCCESS_MIGRATION_LABEL));
    }

    private void bindMigratorProperties() {
        migrator.migrationMessageProperty().addListener((ListChangeListener<String>) c -> {
            if( c.next() && c.getAddedSize() > 0 ) {
                String newMessage = c.getAddedSubList().get(0);
                migrationOutput.appendText(DateUtils.formatMsecPrecision(new Date()) + " - " + newMessage + "\n");
            }
        });

        migrator.sumOfEntityInMongoProperty().addListener((observable, oldValue, newValue) -> {
            FXUtils.runInFxThread(() -> sumOfAllEntityLabel.setText(String.valueOf(newValue)));
        });

        migrator.sumOfFailedEntityMigrationProperty().addListener((observable, oldValue, newValue) -> {
            FXUtils.runInFxThread(() -> sumOfFailedEntityLabel.setText(String.valueOf(newValue)));
        });

        migrator.sumOfMigratedEntityCounterProperty().addListener((observable, oldValue, newValue) -> {
            FXUtils.runInFxThread(() -> sumOfMigratedEntityLabel.setText(String.valueOf(newValue)));
        });

        migrator.setOnSucceeded(event -> {
            NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Migrációs folyamat", "A migrációs folyamat siekresen véget ért!", "");
            changeRunningState(false);
        });

        migrator.setOnFailed(event -> {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Migrációs folyamat", "A migrációs folyamat váratlan hiba miatt véget ért!",
                    event.getSource().getException().getMessage());
            changeRunningState(false);
        });

        migrator.migrationElementProperty().addListener((ListChangeListener<MigrationElement>) c -> {
            Platform.runLater(() -> {
                if( c.next() && c.getAddedSize() != 0 ) {
                    MigrationElement newElement = c.getAddedSubList().get(0);
                    migrationTable.getItems().add( newElement );
                }
            });
        });
    }

    @FXML
    private void handleCancel() {
        migrator.cancel();
        changeRunningState(false);
    }

    private void initTableView() {
        migrationTable.setPlaceholder(new Label(Configuration.Constants.EMPTY_TABLE_MESSAGE));

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setCellFactory(param -> new MigrationTableDateCell());
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityColumn.setCellFactory(param -> new MigrationTableEntityCell());
        successColumn.setCellValueFactory(new PropertyValueFactory<>("success"));
        successColumn.setCellFactory(param -> new MigrationTableSuccessCell());
    }

    private void setDefaultState() {
        migrationTable.getItems().clear();
        migrationOutput.clear();
        changeRunningState(false);
        migrator.reset();
        migrationProgressBar.progressProperty().setValue(0);

        sumOfAllEntityLabel.setText("0");
        sumOfFailedEntityLabel.setText("0");
        sumOfMigratedEntityLabel.setText("0");
    }

    private void changeRunningState(boolean startVisible) {
        startButton.setDisable(startVisible);
        cancelButton.setDisable(!startVisible);
        statBox.setVisible(startVisible);
    }

    @FXML
    private void handleStart() {
        setDefaultState();
        changeRunningState(true);

        if( migrator.getState().equals(Worker.State.READY) ) {
            migrationProgressBar.progressProperty().unbind();
            migrationProgressBar.progressProperty().bind( migrator.progressProperty() );
            migrator.start();
        } else {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Migráció",
                    "A migrációs folyamat nem indítható el!", "A folyamat jelenleg is fut vagy még nem áll készen a futásra.");
        }
    }

    private class MigrationTableDateCell extends TableCell<MigrationElement, Date> {

        public MigrationTableDateCell() {
            super();
        }

        @Override
        protected void updateItem(Date item, boolean empty) {
            super.updateItem(item, empty);

            if( item == null || empty ) {
                setText(null);
                setStyle("");
            } else {
                setText( DateUtils.formatMsecPrecision(item) );
            }
        }
    }

    private class MigrationTableSuccessCell extends TableCell<MigrationElement, Boolean> {

        public MigrationTableSuccessCell() {
            super();
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if( item == null || empty ) {
                setText(null);
                setStyle("");
            } else {
                setText( item ? "Igen" : "Nem" );
                if( !item ) {
                    setTooltip(new Tooltip( getExceptionMessage(getTableRow()) ));
                    setStyle("-fx-background-color: red; -fx-text-fill: white;");
                }
            }
        }

        private String getExceptionMessage(TableRow tableRow) {
            return getTableView().getItems().get(tableRow.getIndex()).getExceptionStackTraceString();
        }

    }

    private class MigrationTableEntityCell extends TableCell<MigrationElement, NamedEnum> {

        public MigrationTableEntityCell() {
            super();
        }

        @Override
        protected void updateItem(NamedEnum item, boolean empty) {
            super.updateItem(item, empty);
            if( item == null || empty ) {
                setText(null);
                setStyle("");
            } else {
                setText( item.getName() );
            }
        }
    }

}