package balint.lenart.controllers.main;

import balint.lenart.model.helper.MigrationElement;
import balint.lenart.model.helper.NamedEnum;
import balint.lenart.services.Migrator;
import balint.lenart.utils.DateUtils;
import balint.lenart.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.Date;

public class MigrationLayoutController {

    private static final Migrator migrator = Migrator.getInstance();

    @FXML private ProgressBar migrationProgressBar;
    @FXML private TableView<MigrationElement> migrationTable;
    @FXML private TableColumn<MigrationElement, Date> timeColumn;
    @FXML private TableColumn<MigrationElement, NamedEnum> entityColumn;
    @FXML private TableColumn<MigrationElement, Boolean> successColumn;
    @FXML private TextArea migrationOutput;

    @FXML
    private void initialize() {
        initTableView();
        setDefaultState();

        migrator.stateProperty().addListener((observable, oldValue, newValue) -> {
            if( !newValue.equals(Worker.State.READY) ) {
                migrationProgressBar.progressProperty().unbind();
                migrationTable.itemsProperty().unbind();
            }
        });

        migrator.exceptionsProperty().addListener((ListChangeListener<Throwable>) c -> {
            Platform.runLater(() -> {
                if( c.next() ) {
                    Throwable exception = c.getAddedSubList().get(0);
                    migrationOutput.appendText(DateUtils.formatMsecPrecision(new Date()) + " - " + exception.getClass().getName() + "\n");
                }
            });
        });

        migrator.setOnSucceeded(event -> {
            NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Migrációs folyamat", "A migrációs folyamat siekresen véget ért!", "Az eltelt idő: " + " ms");
            this.migrationProgressBar.progressProperty().unbind();
            this.migrationProgressBar.setProgress(0.0);
        });

        migrator.setOnFailed(event -> {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Migrációs folyamat", "A migrációs folyamat váratlan hiba miatt véget ért!",
                    event.getSource().getException().getMessage());
        });
    }

    private void initTableView() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setCellFactory(param -> new MigrationTableDateCell());
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityColumn.setCellFactory(param -> new MigrationTableEntityCell());
        successColumn.setCellValueFactory(new PropertyValueFactory<>("success"));
        successColumn.setCellFactory(param -> new MigrationTableSuccessCell());
    }

    private void setDefaultState() {
        migrationProgressBar.progressProperty().unbind();
        migrationProgressBar.setProgress(0D);
        migrationTable.getItems().clear();
        migrationOutput.clear();
    }

    @FXML
    private void handleStart() {
        if( migrator.getState().equals(Worker.State.READY) ) {
            migrationProgressBar.progressProperty().bind( migrator.progressProperty() );
           migrator.migrationElementProperty().addListener((ListChangeListener<MigrationElement>) c -> {
               Platform.runLater(() -> {
                   if( c.next() ) {
                       migrationTable.getItems().add( c.getAddedSubList().get(0) );
                   }
               });
           });
            migrator.start();
        } else {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Migráció",
                    "A migrációs folyamat nem indítható el!", "A folyamat jelenleg is fut.");
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
                    setStyle("-fx-background-color: red");
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