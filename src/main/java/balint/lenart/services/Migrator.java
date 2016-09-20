package balint.lenart.services;

import balint.lenart.dao.mongo.MongoDeviceDAO;
import balint.lenart.dao.mongo.MongoObservationDAO;
import balint.lenart.dao.mongo.MongoUserDAO;
import balint.lenart.dao.postgres.*;
import balint.lenart.model.Device;
import balint.lenart.model.Episode;
import balint.lenart.model.User;
import balint.lenart.model.observations.Observation;
import balint.lenart.model.observations.ObservationType;
import com.google.common.collect.Lists;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class Migrator extends Service<Boolean> {

    private static final Logger LOGGER = Logger.getLogger(Migrator.class);

    private final MongoDeviceDAO mongoDeviceDAO;
    private final MongoObservationDAO mongoObservationDAO;
    private final MongoUserDAO mongoUserDAO;

    private final PostgresDeviceDAO postgresDeviceDAO;
    private final PostgresEpEventDAO postgresEpEventDAO;
    private final PostgresUserDAO postgresUserDAO;
    private final PostgresEpisodeDAO postgresEpisodeDAO;
    private final PostgresMatchingTableDAO postgresMatchingTableDAO;

    private Long sumOfEntityInMongo;

    public static final List<ObservationType> PASSED_TYPES = Lists.newArrayList(
            ObservationType.NOTIFICATION_RECORD, ObservationType.WEIGHT_RECORD, ObservationType.BLOOD_GLUCOSE_RECORD,
            ObservationType.BLOOD_PRESSURE_RECORD, ObservationType.PA_LOG_RECORD, ObservationType.MEDICATION_RECORD
    );

    public Migrator() {
        this.mongoDeviceDAO = new MongoDeviceDAO();
        this.mongoObservationDAO = new MongoObservationDAO();
        this.mongoUserDAO = new MongoUserDAO();
        this.postgresDeviceDAO = new PostgresDeviceDAO();
        this.postgresEpEventDAO = new PostgresEpEventDAO();
        this.postgresUserDAO = new PostgresUserDAO();
        this.postgresEpisodeDAO = new PostgresEpisodeDAO();
        this.postgresMatchingTableDAO = new PostgresMatchingTableDAO();
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
            int failEntityMigration = 0;

            try {
                PostgresConnection.getInstance().setAutoCommit(false);
                List<User> users = mongoUserDAO.getAllUser();
                for(User user : users) {
                    try {
                        User persisted = postgresUserDAO.saveEntity(user);
                        migrateDevices(persisted);
                        PostgresConnection.getInstance().commit();
                    } catch (Exception ex) {
                        // log all exception and continue with next use
                        PostgresConnection.getInstance().rollback();
                        ex.printStackTrace();
                        failEntityMigration++;
                    }
                }
            } catch (Exception ex) {
                // connection or setAutoCommit failure
                ex.printStackTrace();
            } finally {
                try {
                    PostgresConnection.getInstance().setAutoCommit(true);
                } catch (SQLException e) {
                    // ignore this exception
                }
            }

            System.out.println("Fail entity migration: " + failEntityMigration);
            if( failEntityMigration == 0 ) {
                LOGGER.info("Migracio sikeresen vegrehajtva hiba nelkul!");
            } else {
                LOGGER.warn("A migracio soran hiba lepett fel ...");
            }
            return failEntityMigration == 0;
        }

        private Long calculateMigrationsProcess() {
            return mongoDeviceDAO.count() + mongoObservationDAO.count() + mongoUserDAO.count();
        }

        private void migrateDevices(User owner) throws Exception {
            List<Device> devices = mongoDeviceDAO.getDeviceByUser(owner);
            if( devices.isEmpty() ) {
                System.out.println("Found expert user without any devices");
            } else {
                for(Device device : devices) {
                    Device persistedDevice = postgresDeviceDAO.saveEntity(device);
                    Episode persistedEpisode = migrateEpisodes(owner, persistedDevice);
                    postgresMatchingTableDAO.insertToEpisodeDevice(persistedEpisode, persistedDevice);
                    migrateObservations(persistedEpisode, persistedDevice);
                }
            }
        }

        private Episode migrateEpisodes(User user, Device device) throws SQLException {
            Episode episode = new Episode(user, mongoObservationDAO.getLatestObservationDateByDevice(device));
            return postgresEpisodeDAO.saveEntity(episode);
        }

        private void migrateObservations(Episode episode, Device device) throws SQLException {
            List<Observation> observations = mongoObservationDAO.getObservationsByDevice(device);
            observations.forEach(item -> item.setEpisode(episode));

            for(Observation observation : observations) {
                postgresEpEventDAO.saveEntity(observation);
            }
        }
    }

}
