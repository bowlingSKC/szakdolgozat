package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.observations.MissingFood;
import balint.lenart.model.observations.Observation;

import java.sql.*;

public class PostgresEpEventDAO {

    private String getSchemaName() {
        return Configuration.get("postgres.connection.schema");
    }

    public Long count() throws SQLException {
        Statement statement = PostgresConnection.getInstance().getConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM log.ep_event");
        result.next();
        return result.getLong(1);
    }

    public Observation saveEntity(Observation observation) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".ep_event(episode_id, event_type_code, ts_specified, ts_recorded, " +
                        "ts_received, ts_updated, ts_deleted, source_device_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS
        );
        statement.setLong(1, observation.getEpisode().getPostgresId());
        statement.setInt(2, 0);     // FIXME: 2016.09.13. replace this const
        statement.setDate(3, new Date(observation.getTsSpecified().getTime()));
        statement.setDate(4, new Date(observation.getTsRecorded().getTime()));
        statement.setDate(5, new Date(observation.getTsReceived().getTime()));
        if( observation.getTsUpdated() != null ) {
            statement.setDate(6, new Date(observation.getTsUpdated().getTime()));
        } else {
            statement.setDate(6, null);
        }
        if( observation.getTsDeleted() != null ) {
            statement.setDate(7, new Date(observation.getTsDeleted().getTime()));
        } else {
            statement.setDate(7, null);
        }
        statement.setLong(8, observation.getSourceDevice().getPostgresId());
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if( generatedKeys.next() ) {
            observation.setPostgresId( generatedKeys.getLong(1) );
        }

        if( observation instanceof MissingFood ) {
            saveMissingFoodEntity((MissingFood) observation);
        } else {
            throw new RuntimeException("Unhandled observation type: " + observation.getType().getClassName());
        }

        return observation;
    }

    private void saveMissingFoodEntity(MissingFood entity) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_missing_food(event_id, food_id, recipe_id, message_text) " +
                        "VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1, entity.getPostgresId());
        statement.setInt(2, entity.getFoodId());
        statement.setInt(3, entity.getRecipeId());
        statement.setString(4, entity.getMessageText());
        statement.execute();
    }

}
