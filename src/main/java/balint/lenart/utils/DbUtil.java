package balint.lenart.utils;

public class DbUtil {

    public static String getQuotedString(String str) {
        if( str == null ) {
            return null;
        }
        return "'" + str + "'";
    }

}
