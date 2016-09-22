package balint.lenart.services;

import balint.lenart.dao.mongo.MongoDeviceDAO;
import balint.lenart.dao.mongo.MongoObservationDAO;
import balint.lenart.dao.mongo.MongoUserDAO;
import balint.lenart.dao.postgres.*;
import balint.lenart.model.Device;
import balint.lenart.model.Episode;
import balint.lenart.model.User;
import balint.lenart.model.helper.MigrationElement;
import balint.lenart.model.observations.Observation;
import balint.lenart.model.observations.ObservationType;
import balint.lenart.utils.DateUtils;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SyslogAppender;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Observable;

public class Migrator extends Service<Boolean> {

    private static Migrator instance;
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
    private Long sumOfFailedEntityMigration = 0L;

    private ObservableList<Throwable> migrateExceptions = FXCollections.observableArrayList();
    private ObservableList<MigrationElement> migrationElements = FXCollections.observableArrayList();

    public static final List<ObservationType> PASSED_TYPES = Lists.newArrayList(
            ObservationType.NOTIFICATION_RECORD, ObservationType.WEIGHT_RECORD, ObservationType.BLOOD_GLUCOSE_RECORD,
            ObservationType.BLOOD_PRESSURE_RECORD, ObservationType.PA_LOG_RECORD, ObservationType.MEDICATION_RECORD
    );

    private Migrator() {
        this.mongoDeviceDAO = new MongoDeviceDAO();
        this.mongoObservationDAO = new MongoObservationDAO();
        this.mongoUserDAO = new MongoUserDAO();
        this.postgresDeviceDAO = new PostgresDeviceDAO();
        this.postgresEpEventDAO = new PostgresEpEventDAO();
        this.postgresUserDAO = new PostgresUserDAO();
        this.postgresEpisodeDAO = new PostgresEpisodeDAO();
        this.postgresMatchingTableDAO = new PostgresMatchingTableDAO();
    }

    public static Migrator getInstance() {
        if( instance == null ) {
            instance = new Migrator();
        }
        return instance;
    }

    public ObservableList<Throwable> exceptionsProperty() {
        return migrateExceptions;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new MigratorTask();
    }

    public ObservableList<MigrationElement> migrationElementProperty() {
        return migrationElements;
    }

    public class MigratorTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            return startProcess();
        }

        private boolean startProcess() {
            migrationElements.clear();

            updateProgress(0L, 1L);
            sumOfEntityInMongo = calculateMigrationsProcess();

            try {
                PostgresConnection.getInstance().setAutoCommit(false);
                List<User> users = mongoUserDAO.getAllUser();
                for(User user : users) {
                    if( !isCancelled() ) {
                        migrateUsers(user);
                        LOGGER.info(user.getMongoId() + " id-vel rendelkező felhasználó adatai sikeresen migrálva lettek.");
                    } else {
                        updateMessage("A migrálási folyamatot megszakították.");
                        LOGGER.info("A migrálási folyamatot megszakították.");
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

            return sumOfFailedEntityMigration.equals(0L);
        }

        @Override
        protected void updateMessage(String message) {
            super.updateMessage(DateUtils.formatMsecPrecision(new Date()) + " - " + message);
        }

        private void migrateUsers(User user) throws SQLException {
            try {
                User persisted = postgresUserDAO.saveEntity(user);
                migrateDevices(persisted);
                Platform.runLater(() -> updateProgress(getProgress() + 1 , sumOfEntityInMongo));
                Platform.runLater(() ->
                        updateMessage(user.getMongoId() + " id felhasznaló sikeresen migrálva lett."));
                PostgresConnection.getInstance().commit();
                migrationElements.add(new MigrationElement(new Date(), MigrationElement.EntityType.USER, true, null));
            } catch (Exception ex) {
                // log all exception and continue with next use - default use
                PostgresConnection.getInstance().rollback();
                ex.printStackTrace();
                migrateExceptions.add(ex);
                LOGGER.error("Hiba lépett fel " + user.getFullName() + " migrálása közben.", ex);
                sumOfFailedEntityMigration++;
                migrationElements.add(new MigrationElement(new Date(), MigrationElement.EntityType.USER, false, ex));
            }
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
                    migrationElements.add(new MigrationElement(new Date(), MigrationElement.EntityType.DEVICE, true, null));
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
                migrationElements.add(new MigrationElement(new Date(), observation.getType(), true, null));
            }
        }
    }

}
