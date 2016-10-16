package balint.lenart.controllers.main.settings;

import balint.lenart.Configuration;
import balint.lenart.MigrationSettingsLevel;
import balint.lenart.controllers.RefreshTabController;
import balint.lenart.controllers.helper.NamedEnumButtonCell;
import balint.lenart.utils.NotificationUtil;
import com.sun.deploy.config.Config;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.lang3.BooleanUtils;

import java.io.IOException;

public class MigrationSettingsController implements RefreshTabController {

    @FXML private CheckBox dumpBeforeMigrationCheckBox;
    @FXML private CheckBox migrationLogExceptionCheckBox;
    @FXML private CheckBox migrationLogEntityCheckBox;
    @FXML private ComboBox<MigrationSettingsLevel> levelSelector;

    @FXML private CheckBox bloodGlucoseEnabled;
    @FXML private CheckBox bloodPressureEnabled;
    @FXML private CheckBox chgiEnabled;
    @FXML private CheckBox commentEnabled;
    @FXML private CheckBox anamnesisEnabled;
    @FXML private CheckBox labEnabled;
    @FXML private CheckBox mealEnabled;
    @FXML private CheckBox medicationEnabled;
    @FXML private CheckBox missingFoodEnabled;
    @FXML private CheckBox paEnabled;
    @FXML private CheckBox weightEnabled;

    @FXML
    private void initialize() {
        initLevelSelector();
        initQuestionTooltips();
        initItemEnabledBoxes();
        refreshTab();
    }

    private void initItemEnabledBoxes() {
        bloodGlucoseEnabled.setSelected( Configuration.getBoolean("migration.items.bloodglucose") );
        bloodPressureEnabled.setSelected( Configuration.getBoolean("migration.items.bloodpressure") );
        chgiEnabled.setSelected( Configuration.getBoolean("migration.items.chgi") );
        commentEnabled.setSelected( Configuration.getBoolean("migration.items.comment") );
        anamnesisEnabled.setSelected( Configuration.getBoolean("migration.items.dietlog") );
        labEnabled.setSelected( Configuration.getBoolean("migration.items.lab") );
        mealEnabled.setSelected( Configuration.getBoolean("migration.items.meal") );
        medicationEnabled.setSelected( Configuration.getBoolean("migration.items.medication") );
        missingFoodEnabled.setSelected( Configuration.getBoolean("migration.items.missingfood") );
        paEnabled.setSelected( Configuration.getBoolean("migration.items.pa") );
        weightEnabled.setSelected( Configuration.getBoolean("migration.items.weight") );
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

        Configuration.set("migration.items.bloodglucose", BooleanUtils.toStringTrueFalse(bloodGlucoseEnabled.isSelected()));
        Configuration.set("migration.items.bloodpressure", BooleanUtils.toStringTrueFalse(bloodPressureEnabled.isSelected()));
        Configuration.set("migration.items.chgi", BooleanUtils.toStringTrueFalse(chgiEnabled.isSelected()));
        Configuration.set("migration.items.comment", BooleanUtils.toStringTrueFalse(commentEnabled.isSelected()));
        Configuration.set("migration.items.lab", BooleanUtils.toStringTrueFalse(labEnabled.isSelected()));
        Configuration.set("migration.items.meal", BooleanUtils.toStringTrueFalse(mealEnabled.isSelected()));
        Configuration.set("migration.items.medication", BooleanUtils.toStringTrueFalse(medicationEnabled.isSelected()));
        Configuration.set("migration.items.pa", BooleanUtils.toStringTrueFalse(paEnabled.isSelected()));
        Configuration.set("migration.items.weight", BooleanUtils.toStringTrueFalse(weightEnabled.isSelected()));

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
        dumpBeforeMigrationCheckBox.setSelected(Configuration.getBoolean("backup.createBeforeMigration"));
        migrationLogExceptionCheckBox.setSelected(Configuration.getBoolean("migration.show.exceptions"));
        migrationLogEntityCheckBox.setSelected(Configuration.getBoolean("migration.show.entities"));
        levelSelector.getSelectionModel().select(Configuration.getMigrationLevel());
    }
}
