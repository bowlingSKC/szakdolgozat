package balint.lenart.dump;

import balint.lenart.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MongoDump implements DatabaseDump {

    @Override
    public void backup() throws IOException {
        final String backupCommand = createBackupCommand();
        final String filePath = createBackupFilePath();

        Runtime runtime = Runtime.getRuntime();
        runtime.exec(backupCommand + "--out " + filePath);

        System.out.println(backupCommand + "--out " + filePath);
    }

    private String createBackupFilePath() {
        String filePath = "backups/mongo/backup-lavinia-";
        filePath += new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        filePath += ".json";
        return filePath;
    }

    private String createBackupCommand() {
        final StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("mongodump ");
        commandBuilder.append("--host ").append(Configuration.get("mongo.connection.host")).append(" ");
        commandBuilder.append("--port ").append(Configuration.get("mongo.connection.port")).append(" ");
        commandBuilder.append("--username ").append(Configuration.get("mongo.connection.username")).append(" ");
        commandBuilder.append("--password ").append(Configuration.get("mongo.connection.password")).append(" ");
        return commandBuilder.toString();
    }

    @Override
    public void restore() {
        throw new UnsupportedOperationException("Not implement yet.");
    }
}