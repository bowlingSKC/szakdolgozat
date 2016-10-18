package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.User;
import balint.lenart.utils.DbUtil;
import balint.lenart.utils.Tuple;
import balint.lenart.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.*;
import java.util.Random;

public class PostgresUserDAO {

    private String getSchemaName() {
        return Configuration.get("postgres.connection.schema");
    }

    private String getTableName() {
        return getSchemaName() + ".user";
    }

    public Long count() throws SQLException {
        Statement statement = PostgresConnection.getInstance().getConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM log.user");
        result.next();
        return result.getLong(1);
    }

    public User saveEntity(User user) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement("" +
                "INSERT INTO " + getTableName() + "(user_type_code, firstname, family_name, email, ds_id, user_desc) VALUES" +
                "(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);

        statement.setInt(1, 0);
        statement.setString(2, nameByUser.getFirst());
        statement.setString(3, nameByUser.getSecond());
        statement.setString(4, user.getEmail());
        statement.setString(5, user.getMongoId());
        if(StringUtils.isNotEmpty(user.getComment())) {
            statement.setString(6, user.getComment());
        } else {
            statement.setNull(6, Types.VARCHAR);
        }
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if( generatedKeys.next() ) {
            user.setPostgresId( generatedKeys.getLong(1) );
        }

        return user;
    }

    public User getUserByMongoId(String mongoId) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "SELECT user_id, user_type_code, firstname, family_name, email, ds_id, user_desc FROM " + getTableName() + " WHERE ds_id = ?"
        );
        statement.setString(1, mongoId);
        ResultSet resultSet = statement.executeQuery();

        if( resultSet.next() ) {
            User user = new User();
            user.setPostgresId( resultSet.getLong("user_id") );
            user.setComment( resultSet.getString("user_desc") );
            user.setFullName( resultSet.getString("firstname") + resultSet.getString("family_name") );
            user.setEmail( resultSet.getString("email") );
            user.setMongoId( resultSet.getString("ds_id") );

            return user;
        } else {
            return null;
        }
    }

    public void addToExpertUser(User user) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".expert_user(user_id) VALUES (?)"
        );
        statement.setLong(1, user.getPostgresId());
        statement.execute();
    }

}
