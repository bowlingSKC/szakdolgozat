package balint.lenart.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat MSEC_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    public static String formatMsecPrecision(Date date) {
        return MSEC_FORMAT.format(date);
    }

    public static String formatDateToLogFile(Date date) {
        return new SimpleDateFormat("yyy-MM-dd-HH-mm-ss").format(date);
    }

}
