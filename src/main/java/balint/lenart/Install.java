package balint.lenart;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Install {

    private static final Logger LOGGER = Logger.getLogger(Install.class);

    public void run() {
        createBackupDirectories();
        readConfigurations();
        createLogDirectories();

        LOGGER.trace("A szükséges könyvtárak rendelkezésre állnak");
    }

    private void readConfigurations() {
        try {
            Configuration.loadFromFile();
            LOGGER.trace("A beállításokat sikersen be lettek olvasva");
        } catch (IOException ex) {
            LOGGER.error("A beállításokat tartalmazó fájlt nem lehet beolvasni és/vagy létrehozni", ex);
            throw new RuntimeException("Nem lehet beolvasni és létrehozni a beállításokat tartalmazó file-t!");
        }
    }

    private void createBackupDirectories() {
        File backupDirectory = new File("backups");
        if(!backupDirectory.isDirectory()) {
            backupDirectory.mkdir();
        }
        File postgresBackup = new File("backups/postgres");
        if(!postgresBackup.isDirectory()) {
            postgresBackup.mkdir();
        }
        File mongoBackup = new File("backups/mongo");
        if(!mongoBackup.isDirectory()) {
            mongoBackup.mkdir();
        }
    }

    private void createLogDirectories() {
        File logDirectory = new File("logs/migrations");
        if(!logDirectory.isDirectory()) {
            logDirectory.mkdirs();
        }
    }

    private String createOsIndependetPath(String ... directories) {
        return String.join(File.separator, directories);
    }

}
