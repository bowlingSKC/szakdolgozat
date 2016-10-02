package balint.lenart.log;

import balint.lenart.utils.DateUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.util.Date;

// http://veerasundar.com/blog/2009/08/how-to-create-a-new-log-file-for-each-time-the-application-runs/
public class NewLogForEachRunFileAppender extends FileAppender {

    public NewLogForEachRunFileAppender() {
        super();
    }

    public NewLogForEachRunFileAppender(Layout layout, String filename, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
        super(layout, filename, append, bufferedIO, bufferSize);
    }

    public NewLogForEachRunFileAppender(Layout layout, String filename, boolean append) throws IOException {
        super(layout, filename, append);
    }

    public NewLogForEachRunFileAppender(Layout layout, String filename) throws IOException {
        super(layout, filename);
    }

    @Override
    public void activateOptions() {
        if( fileName != null ) {
            try {
                fileName = getNewLogFileName();
                setFile(fileName, fileAppend, bufferedIO, bufferSize);
            } catch (Exception ex) {
                errorHandler.error("Error while activating log options ", ex, ErrorCode.FILE_OPEN_FAILURE);
            }
        }
    }

    private String getNewLogFileName() {
        if (fileName != null) {
            final File logFile = new File(fileName);
            final String fileName = logFile.getName();
            String newFileName = null;

            final int dotIndex = fileName.indexOf(".");
            String dateTimeString = DateUtils.formatDateToLogFile(new Date());
            if (dotIndex != -1) {
                newFileName = fileName.substring(0, dotIndex) + "-" + dateTimeString + "."
                        + fileName.substring(dotIndex +1);
            } else {
                newFileName = fileName + "-" + dateTimeString;
            }
            return logFile.getParent() +  File.separator +  newFileName;
        }
        return null;
    }
}
