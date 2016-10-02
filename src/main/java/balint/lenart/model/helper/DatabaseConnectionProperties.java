package balint.lenart.model.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConnectionProperties {

    private String host;
    private int port = 5432;    // Default port
    private String dbName;
    private String userName;
    private String password;

    public String getJDBCConnectionUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

}
