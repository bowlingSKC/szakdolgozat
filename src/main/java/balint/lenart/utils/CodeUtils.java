package balint.lenart.utils;

import org.apache.commons.lang3.StringUtils;

public class CodeUtils {

    private static final String UNSPEC_VALUE = "unspec";

    public static Integer getMedicationAdminRouteCode(String value) {
        if(StringUtils.isEmpty(value) || UNSPEC_VALUE.equals(value)) {
            return null;
        }

        switch (value) {
            case "Implant":
                return 1;
            case "Inhal":
                return 2;
            case "N":
                return 3;
            case "Instill":
                return 4;
            case "O":
                return 5;
            case "P":
                return 6;
            case "R":
                return 7;
            case "SL":
                return 8;
            case "TD":
                return 9;
            case "V":
                return 10;
            default:
                throw new RuntimeException("Unspec code: [medication] " + value);
        }
    }

    public static Integer getMedicationTimeOfAdministration(Integer value) {
        if( value == null || value > 2 ) {
            return null;
        }
        return value;
    }

}
