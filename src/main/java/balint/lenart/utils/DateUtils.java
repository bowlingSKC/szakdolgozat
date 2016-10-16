package balint.lenart.utils;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat MSEC_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    public static String formatMsecPrecision(Date date) {
        return MSEC_FORMAT.format(date);
    }

    public static String formatDateToLogFile(Date date) {
        return new SimpleDateFormat("yyy-MM-dd-HH-mm-ss").format(date);
    }

    public static boolean isSameDay(final Date date1, final Date date2) {
        if( date1 == null || date2 == null ) {
            return false;
        }

        DateTime dateTime1 = new DateTime(date1);
        DateTime dateTime2 = new DateTime(date2);
        return dateTime1.getYear() == dateTime2.getYear() && dateTime1.getDayOfYear() == dateTime2.getDayOfYear();
    }

    public static Date formatMealTimestampDate(String str) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX", Locale.ENGLISH);
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
