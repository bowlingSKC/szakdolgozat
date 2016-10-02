package balint.lenart.services;

import balint.lenart.log.Log;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogService {

    private static final Pattern APPLICATION_LOG_NAME_PATTERN =
            Pattern.compile("^log-migrator-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.log$");
    private static final Pattern MIGRATION_LOG_NAME_PATTERN =
            Pattern.compile("^log-migration-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.log$");

    private boolean matchesFileNameForLogType(Log.LogType logType, String fileName) {
        if(Log.LogType.APPLICATION.equals(logType)) {
            return APPLICATION_LOG_NAME_PATTERN.matcher(fileName).matches();
        } else if(Log.LogType.MIGRATION.equals(logType)) {
            return MIGRATION_LOG_NAME_PATTERN.matcher(fileName).matches();
        }

        throw new RuntimeException("Unhandled LogType");
    }

    private List<File> getApplicationLogFiles() {
        File logDir = new File("logs");
        return Lists.newArrayList(logDir.listFiles())
                .stream()
                .filter(file -> matchesFileNameForLogType(Log.LogType.APPLICATION, file.getName()))
                .collect(Collectors.toList());
    }

    private List<File> getMigrationLogFiles() {
        File logDir = new File("logs/migrations");
        return Lists.newArrayList(logDir.listFiles())
                .stream()
                .filter(file -> matchesFileNameForLogType(Log.LogType.MIGRATION, file.getName()))
                .collect(Collectors.toList());
    }

    public List<Log> getApplicationLogs() {
        List<Log> logs = Lists.newArrayList();
        List<File> logFiles = getApplicationLogFiles();
        for(File logFile : logFiles) {
            Log log = createLogFromFile(logFile);
            if ( log != null) {
                logs.add(log);
            }
        }
        return logs;
    }

    public List<Log> getMigrationLogs() {
        List<Log> logs = Lists.newArrayList();
        List<File> logFiles = getMigrationLogFiles();
        for (File logFile : logFiles) {
            Log log = createLogFromFile(logFile);
            if ( log != null) {
                logs.add(log);
            }
        }
        return logs;
    }

    private Log createLogFromFile(File file) {
        try {
            return new Log( file.getName(), file.getName(), FileUtils.readFileToString(file) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
