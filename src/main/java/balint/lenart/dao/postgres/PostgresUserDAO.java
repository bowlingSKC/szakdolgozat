package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.User;
import balint.lenart.utils.DbUtil;
import balint.lenart.utils.Tuple;
import balint.lenart.utils.UserUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class PostgresUserDAO {

    private String getTableName() {
        return Configuration.get("postgres.connection.schema") + ".user";
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
        statement.setString(6, user.getComment());
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if( generatedKeys.next() ) {
            user.setPostgresId( generatedKeys.getLong(1) );
        }

        return user;
    }

}
