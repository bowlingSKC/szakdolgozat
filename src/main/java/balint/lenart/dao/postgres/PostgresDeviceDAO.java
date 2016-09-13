package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.Device;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresDeviceDAO {

    public Long count() throws SQLException {
        Statement statement = PostgresConnection.getInstance().getConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM log.device");
        result.next();
        return result.getLong(1);
    }

    public Device saveEntity(Device device) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getTableName() + "(hw_serial, input_type_code, ds_device_id) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, device.getHwId());
        statement.setInt(2, 0);     // FIXME: 2016.09.12. replace this constant
        statement.setString(3, device.getMongoId());
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if( generatedKeys.next() ) {
            device.setPostgresId( generatedKeys.getLong(1) );
        }
        return device;
    }

    private String getTableName() {
        return Configuration.get("postgres.connection.schema") + ".device";
    }

}
