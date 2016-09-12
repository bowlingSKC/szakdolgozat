package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgresMatchingTableDAO {

    private static String getSchema() {
        return Configuration.get("postgres.connection.schema");
    }

    public void insertToEpisodeDevice(Episode episode, Device device) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchema() + ".episode_device(episode_id, device_id) VALUES (?, ?);");
        statement.setLong(1, episode.getPostgresId());
        statement.setLong(2, device.getPostgresId());
        statement.execute();
    }

}
