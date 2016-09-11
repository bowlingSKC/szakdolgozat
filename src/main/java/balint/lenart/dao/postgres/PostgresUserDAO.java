package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.User;
import balint.lenart.utils.DbUtil;
import balint.lenart.utils.Tuple;
import balint.lenart.utils.UserUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    public void saveEntity(User user) throws SQLException {
        Statement statement = PostgresConnection.getInstance().getConnection().createStatement();
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);
        statement.execute(
                "INSERT INTO " + getTableName() + "(user_type_code, firstname, family_name, email, ds_id, user_desc) VALUES (" +
                        0 + ", " +
                        DbUtil.getQuotedString(nameByUser.getFirst()) + "," +
                        DbUtil.getQuotedString(nameByUser.getSecond()) + ", " +
                        DbUtil.getQuotedString(user.getEmail()) + ", " +
                        DbUtil.getQuotedString(user.getMongoId()) + ", " +
                        DbUtil.getQuotedString(user.getComment()) +
                        ");"
        );
    }

}
