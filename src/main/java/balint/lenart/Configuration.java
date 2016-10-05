package balint.lenart;

import balint.lenart.model.helper.DatabaseConnectionProperties;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class Configuration {

    private static final File SETTINGS_FILE = new File("settings.properties");
    private static final Map<String, String> SETTINGS = Maps.newHashMap();

    private Configuration() {

    }

    public static void loadFromFile() throws IOException {
        if( !SETTINGS_FILE.exists() ) {
            createDefaultConfiguration();
        }

        Properties settings = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(SETTINGS_FILE);
            settings.load(inputStream);

            settings.forEach((key, value) -> SETTINGS.put((String)key, (String)value));
        } catch (IOException e) {
            e.printStackTrace();
            createDefaultConfiguration();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveToFile() throws IOException {
        SETTINGS_FILE.deleteOnExit();
        SETTINGS_FILE.createNewFile();

        Properties properties = new Properties();
        SETTINGS.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> {
            properties.setProperty(entry.getKey(), entry.getValue());
        });

        OutputStream os = new FileOutputStream(SETTINGS_FILE);
        properties.store(os, null);
    }

    public static String get(String key) {
        return SETTINGS.get(key);
    }

    public static Integer getInt(String key) {
        return Integer.valueOf(get(key));
    }

    public static void set(String key, String value) {
        SETTINGS.put(key, value);
    }

    public static DatabaseConnectionProperties getDefaultMongoConnectionProperties() {
        return new DatabaseConnectionProperties(
                get("mongo.connection.database"),
                getInt("mongo.connection.port"),
                get("mongo.connection.database"),
                get("mongo.connection.username"),
                get("mongo.connection.password")
        );
    }

    public static DatabaseConnectionProperties getDefaultPostgresConnectionProperties() {
        return new DatabaseConnectionProperties(
                get("postgres.connection.host"),
                getInt("postgres.connection.port"),
                get("postgres.connection.database"),
                get("postgres.connection.username"),
                get("postgres.connection.password")
        );
    }

    public static MigrationSettingsLevel getMigrationLevel() {
        return MigrationSettingsLevel.valueOf(get("migration.tranlevel"));
    }

    private static void createDefaultConfiguration() throws IOException {
        URL defaultSettingsFileUrl = Configuration.class.getClassLoader().getResource("default-settings.properties");
        System.out.println(defaultSettingsFileUrl);
        FileUtils.copyURLToFile(defaultSettingsFileUrl, SETTINGS_FILE);

    }

    public static void setMigrationLevel(MigrationSettingsLevel migrationLevel) {
        SETTINGS.put("migration.tranlevel", migrationLevel.name());
    }

    public static boolean getBoolean(String key) {
        return BooleanUtils.toBoolean(get(key));
    }

    public interface Constants  {
        String WINDOW_TITLE = "Lavinia adatbázis migráló";          // JavaFX Window title
        String EMPTY_TABLE_MESSAGE = "Nincs megjeleníthető adat";   // TableView default empty caption
        double WINDOW_MIN_WIDTH = 650;                              // Window min width size
        double WINDOW_MIN_HEIGHT = 450;                             // Window min height size

        String PBAR_TOOLTIP = "A folyamat állapotát mutatja.\n" +
                "Az összes MongoDB-beli dokumentumra vetíti a folyamatot, így hibás entitás esetén csekély értékkel " +
                "eltérhet a valódi állapottól.";                    // ProgressBar tooltip in Migration tab
        String SUCCESS_MIGRATION_LABEL = "Sikeresen migrált entitások száma";
        String FAILED_MIGRATION_LABEL = "Sikertelenül migrált entitások száma\n" +
                "Nem veszi figyelembe a sikertelenül migrált entitáshoz tatrozó további entitások számát";
        String ALL_MIGRATION_LABEL = "Az összes entitások száma MondoDB-ben";
    }
}
