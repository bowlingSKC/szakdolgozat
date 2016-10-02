package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import org.apache.log4j.Logger;

import java.sql.*;

public class PostgresConnection {

    private static Logger LOGGER = Logger.getLogger(PostgresConnection.class);
    private static PostgresConnection instance;
    private Connection connection;

    protected PostgresConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://" + Configuration.get("postgres.connection.host") + ":" + Configuration.get("postgres.connection.port") + "/"
                            + Configuration.get("postgres.connection.database"),
                    Configuration.get("postgres.connection.username"),
                    Configuration.get("postgres.connection.password")
            );
            LOGGER.info("A Postgres adatbázissal sikerült felvenni a kapcsolatot!");
        } catch (Exception ex) {
            LOGGER.error("A Postgres adatbázissal nem lehet felvenni a kapcsolatot!", ex);
            throw ex;
        }
    }

    public static PostgresConnection getInstance() throws SQLException {
        if( instance == null ) {
            instance = new PostgresConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setAutoCommit(boolean value) throws SQLException {
        connection.setAutoCommit(value);
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        connection.rollback(savepoint);
    }

    public Savepoint setSavepoint() throws SQLException {
        return connection.setSavepoint();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.close();
        LOGGER.info("A Postgres adatbázissal való kapcsolat bontva!");
    }
}
