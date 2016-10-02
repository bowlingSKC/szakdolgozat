package balint.lenart.controllers.main.settings;

import balint.lenart.Configuration;
import balint.lenart.MigrationSettingsLevel;
import balint.lenart.controllers.RefreshTabController;
import balint.lenart.controllers.helper.NamedEnumButtonCell;
import balint.lenart.utils.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.lang3.BooleanUtils;

import java.io.IOException;

public class MigrationSettingsController implements RefreshTabController {

    @FXML private CheckBox dumpBeforeMigrationCheckBox;
    @FXML private CheckBox migrationLogExceptionCheckBox;
    @FXML private CheckBox migrationLogEntityCheckBox;
    @FXML private ComboBox<MigrationSettingsLevel> levelSelector;

    @FXML
    private void initialize() {
        initLevelSelector();
        initQuestionTooltips();
        refreshTab();
    }

    private void initQuestionTooltips() {
        Tooltip.install(dumpBeforeMigrationCheckBox,
                new Tooltip("A rendszer biztonsági mentést késztít a Postgres adatbázis megfelelő sémájáról a migráció előtt."));
        Tooltip.install(migrationLogExceptionCheckBox,
                new Tooltip("Migrálás közben a keletkezett kivételek és hibák azonnali jelzése a felületen.\n" +
                        "Kis mértékben rontja a migráció teljesítményét."));
        Tooltip.install(migrationLogEntityCheckBox,
                new Tooltip("Migrálás közben az egyes entitások sikerességének azonnali visszajelzése a felületen.\n" +
                        "Nagy mértékben rontja a migráció teljesítményét."));
        Tooltip.install(levelSelector,
                new Tooltip("A migrálás tranzakció szintje.\n" +
                        "Megfigyelés: csak a hiányzó/rossz megfigyeléseket hagyjuk figyelmen kívül a migráció során\n" +
                        "Eszköz: csak azok az eszközök kerülnek mentésre, melyekhez nem tartozik hibás megfigyelés\n" +
                        "Felhasználó: csak azok a felhasználók kerülnek mentésre, melyekhez nem tartozik hiányos/rossz adat"));
    }

    private void initLevelSelector() {
        levelSelector.getItems().addAll( MigrationSettingsLevel.values() );
        levelSelector.setCellFactory(param -> new NamedEnumButtonCell<>());
        levelSelector.setButtonCell(new NamedEnumButtonCell<>());
    }

    @FXML
    private void handleSave() {
        Configuration.set("backup.createBeforeMigration", BooleanUtils.toStringTrueFalse(dumpBeforeMigrationCheckBox.isSelected()));
        Configuration.set("migration.show.exceptions", BooleanUtils.toStringTrueFalse(migrationLogExceptionCheckBox.isSelected()));
        Configuration.set("migration.show.entities", BooleanUtils.toStringTrueFalse(migrationLogEntityCheckBox.isSelected()));
        Configuration.setMigrationLevel(levelSelector.getSelectionModel().getSelectedItem());

        try {
            Configuration.saveToFile();
            NotificationUtil.showNotification(Alert.AlertType.INFORMATION, "Beállítások",
                    "Sikeres mentés!", "Az adatok sikeresen mentve lettek!");
        } catch (IOException ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba", "A következő hibák léptek fel mentés közben:",
                    ex.getMessage());
        }
    }

    @Override
    public void refreshTab() {
        dumpBeforeMigrationCheckBox.setSelected(BooleanUtils.toBoolean(Configuration.get("backup.createBeforeMigration")));
        migrationLogExceptionCheckBox.setSelected(BooleanUtils.toBoolean(Configuration.get("migration.show.exceptions")));
        migrationLogEntityCheckBox.setSelected(BooleanUtils.toBoolean(Configuration.get("migration.show.entities")));
        levelSelector.getSelectionModel().select(Configuration.getMigrationLevel());
    }
}
