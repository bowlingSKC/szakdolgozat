package balint.lenart;

import java.io.File;
import java.io.IOException;

public class Install {

    public void run() {
        createBackupDirectories();
        readConfigurations();
    }

    private void readConfigurations() {
        try {
            Configuration.loadFromFile();
        } catch (IOException e) {
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


}
