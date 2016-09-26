package balint.lenart.utils;

import balint.lenart.model.helper.PostgresConnectionProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class DbUtil {

    private static final Map<Integer, String> SQL_TYPES = new HashMap<Integer, String>() {
        {
            put(-7, "BIT");
            put(-6, "TINYINT");
            put(-5, "BIGINT");
            put(-4, "LONGVARBINARY");
            put(-3, "VARBINARY");
            put(-2, "BINARY");
            put(-1, "LONGVARCHAR");
            put(0, "NULL");
            put(1, "CHAR");
            put(2, "NUMERIC");
            put(3, "DECIMAL");
            put(4, "INTEGER");
            put(5, "SMALLINT");
            put(6, "FLOAT");
            put(7, "REAL");
            put(8, "DOUBLE");
            put(12, "VARCHAR");
            put(91, "DATE");
            put(92, "TIME");
            put(93, "TIMESTAMP");
            put(1111, "OTHER");
        }
    };

    public static String getJdbcTypeName(Integer jdbcType) {
        return SQL_TYPES.get(jdbcType);
    }

    public static boolean testPostgresConnection(PostgresConnectionProperties properties) {
        try {
            Connection connection =
                    DriverManager.getConnection(properties.getJDBCConnectionUrl(), properties.getUserName(), properties.getPassword());
            connection.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
