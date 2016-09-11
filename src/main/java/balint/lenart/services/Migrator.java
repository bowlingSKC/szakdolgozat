package balint.lenart.services;

import balint.lenart.dao.mongo.MongoDeviceDAO;
import balint.lenart.dao.mongo.MongoObservationDAO;
import balint.lenart.dao.mongo.MongoUserDAO;
import balint.lenart.dao.postgres.PostgresConnection;
import balint.lenart.dao.postgres.PostgresDeviceDAO;
import balint.lenart.dao.postgres.PostgresEpEventDAO;
import balint.lenart.dao.postgres.PostgresUserDAO;
import balint.lenart.model.User;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.List;

public class Migrator extends Service<Boolean> {

    private final MongoDeviceDAO mongoDeviceDAO;
    private final MongoObservationDAO mongoObservationDAO;
    private final MongoUserDAO mongoUserDAO;

    private final PostgresDeviceDAO postgresDeviceDAO;
    private final PostgresEpEventDAO postgresEpEventDAO;
    private final PostgresUserDAO postgresUserDAO;

    private Long sumOfEntityInMongo;

    public Migrator() {
        this.mongoDeviceDAO = new MongoDeviceDAO();
        this.mongoObservationDAO = new MongoObservationDAO();
        this.mongoUserDAO = new MongoUserDAO();
        this.postgresDeviceDAO = new PostgresDeviceDAO();
        this.postgresEpEventDAO = new PostgresEpEventDAO();
        this.postgresUserDAO = new PostgresUserDAO();
    }

    @Override
    protected Task<Boolean> createTask() {
        return new MigrateTask();
    }

    private class MigrateTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            return startProgress();
        }

        private boolean startProgress() {
            sumOfEntityInMongo = calculateMigrationsProcess();

            try {
                PostgresConnection.getInstance().setAutoCommit(false);
                List<User> users = mongoUserDAO.getAllUser();
                for(User user : users) {
                    try {
                        postgresUserDAO.saveEntity(user);
                        updateProgress();
                        PostgresConnection.getInstance().commit();
                    } catch (Exception ex) {
                        // log all exception and continue with next user
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    PostgresConnection.getInstance().setAutoCommit(true);
                } catch (SQLException e) {
                    // ignore this exception
                }
            }

            return false;
        }

        private void updateProgress() {
            //updateProgress( getProgress() + 1, sumOfEntityInMongo );
        }

        private Long calculateMigrationsProcess() {
            return mongoDeviceDAO.count() + mongoObservationDAO.count() + mongoUserDAO.count();
        }
    }

}
