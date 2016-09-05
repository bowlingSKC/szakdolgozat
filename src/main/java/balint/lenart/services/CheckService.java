package balint.lenart.services;


import balint.lenart.dao.mongo.MongoDeviceDAO;
import balint.lenart.dao.mongo.MongoObservationDAO;
import balint.lenart.dao.mongo.MongoUserDAO;
import balint.lenart.dao.postgres.PostgresDeviceDAO;
import balint.lenart.dao.postgres.PostgresEpEventDAO;
import balint.lenart.dao.postgres.PostgresUserDAO;

import java.sql.SQLException;

public class CheckService {

    private final MongoUserDAO mongoMongoUserDAO;
    private final MongoDeviceDAO mongoMongoDeviceDAO;
    private final MongoObservationDAO mongoMongoObservationDAO;

    private final PostgresUserDAO postgresPostgresUserDAO;
    private final PostgresDeviceDAO postgresPostgresDeviceDAO;
    private final PostgresEpEventDAO postgresPostgresEpEventDAO;

    public CheckService() {
        this.mongoMongoUserDAO = new MongoUserDAO();
        this.mongoMongoDeviceDAO = new MongoDeviceDAO();
        this.mongoMongoObservationDAO = new MongoObservationDAO();

        this.postgresPostgresDeviceDAO = new PostgresDeviceDAO();
        this.postgresPostgresUserDAO = new PostgresUserDAO();
        this.postgresPostgresEpEventDAO = new PostgresEpEventDAO();
    }

    public ShallowMigrationHelper shallowCheckMigration() throws SQLException {
        ShallowMigrationHelper helper = new ShallowMigrationHelper();
        helper.usersInMongo = mongoMongoUserDAO.count();
        helper.usersInPostgres = postgresPostgresUserDAO.count();
        helper.deviceInMongo = mongoMongoDeviceDAO.count();
        helper.deviceInPostgres = postgresPostgresDeviceDAO.count();
        helper.observationInMongo = mongoMongoObservationDAO.count();
        helper.observationInPostgres = postgresPostgresEpEventDAO.count();
        return helper;
    }

    public void deepCheckMigration() throws SQLException {

    }

    public static class ShallowMigrationHelper {
        public long usersInPostgres;
        public long usersInMongo;
        public long deviceInPostgres;
        public long deviceInMongo;
        public long observationInPostgres;
        public long observationInMongo;

        public boolean needMigration() {
            return usersInMongo > usersInPostgres ||
                    deviceInMongo > deviceInPostgres ||
                    observationInMongo > observationInPostgres;
        }

        @Override
        public String toString() {
            return "ShallowMigrationHelper{" +
                    "usersInPostgres=" + usersInPostgres +
                    ", usersInMongo=" + usersInMongo +
                    ", deviceInPostgres=" + deviceInPostgres +
                    ", deviceInMongo=" + deviceInMongo +
                    ", observationInPostgres=" + observationInPostgres +
                    ", observationInMongo=" + observationInMongo +
                    ", needMigration= + " + needMigration()
                    + "}";
        }
    }

}
