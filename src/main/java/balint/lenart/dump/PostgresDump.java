package balint.lenart.dump;

import balint.lenart.Configuration;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostgresDump implements DatabaseDump {

    @Override
    public void backup() throws IOException {
        final String backupCommand = createBackupCommand();
        final String filePath = createBackupFilePath();

        Runtime runtime = Runtime.getRuntime();
        Process backupProcess = runtime.exec(backupCommand + " > " + filePath);
    }

    @Override
    public void restore() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // from http://stackoverflow.com/questions/2893954/how-to-pass-in-password-to-pg-dump
    private String createBackupCommand() {
        final StringBuffer commandBuffer = new StringBuffer();
        commandBuffer.append("pg_dump  ");
        commandBuffer.append("--dbname=postgresql://")
                .append(Configuration.get("postgres.connection.username"))
                .append(":")
                .append(Configuration.get("postgres.connection.password"))
                .append("@")
                .append(Configuration.get("postgres.connection.host"))
                .append("/")
                .append(Configuration.get("postgres.connection.database"))
                .append(" ");
        commandBuffer.append("--schema ").append(Configuration.get("postgres.connection.schema"));
        return commandBuffer.toString();
    }

    private String createBackupFilePath() throws IOException {
        String filePath = "backups/postgres/backup-log-schema-";
        filePath += new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        filePath += ".sql";
        new File(filePath).createNewFile();
        return filePath;
    }

    public List<String> getBackupFiles() {
        List<String> files = Lists.newArrayList();
        File postgresBackups = new File("backups/postgres");
        for(File file : postgresBackups.listFiles()) {
            if( file.getName().endsWith(".sql") ) {
                files.add(file.getName());
            }
        }
        return files;
    }

}
