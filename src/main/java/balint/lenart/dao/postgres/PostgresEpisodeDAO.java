package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.Episode;

import java.sql.*;

public class PostgresEpisodeDAO {

    private static String getTableName() {
        return Configuration.get("postgres.connection.schema") + ".episode";
    }

    public Episode saveEntity(Episode episode) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getTableName() + "(user_id, start_date, ep_type_code) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1, episode.getUser().getPostgresId());
        if( episode.getStartDate() != null ) {
            statement.setTimestamp(2, new Timestamp(episode.getStartDate().getTime()));
        } else {
            statement.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
        }

        statement.setInt(3, 0); // FIXME: 2016.09.12. replace this cons
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if( generatedKeys.next() ) {
            episode.setPostgresId(generatedKeys.getLong(1));
        }
        return episode;
    }

}
