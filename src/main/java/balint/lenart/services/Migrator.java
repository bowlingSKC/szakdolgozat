package balint.lenart.services;

import balint.lenart.Configuration;
import balint.lenart.dao.mongo.MongoDeviceDAO;
import balint.lenart.dao.mongo.MongoObservationDAO;
import balint.lenart.dao.mongo.MongoUserDAO;
import balint.lenart.dao.postgres.*;
import balint.lenart.model.Device;
import balint.lenart.model.Episode;
import balint.lenart.model.User;
import balint.lenart.model.helper.MigrationElement;
import balint.lenart.model.helper.NamedEnum;
import balint.lenart.model.observations.Observation;
import balint.lenart.model.observations.ObservationType;
import balint.lenart.utils.DateUtils;
import balint.lenart.utils.NotificationUtil;
import com.google.common.collect.Lists;
import com.sun.deploy.config.Config;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Migrator extends Service<Boolean> {

    private static Migrator instance;
    private static final Logger LOGGER = Logger.getLogger(Migrator.class);

    // Mongo database DAOs
    private final MongoDeviceDAO mongoDeviceDAO;
    private final MongoObservationDAO mongoObservationDAO;
    private final MongoUserDAO mongoUserDAO;

    // Postgres database DAOs
    private final PostgresDeviceDAO postgresDeviceDAO;
    private final PostgresEpEventDAO postgresEpEventDAO;
    private final PostgresUserDAO postgresUserDAO;
    private final PostgresEpisodeDAO postgresEpisodeDAO;
    private final PostgresMatchingTableDAO postgresMatchingTableDAO;

    // Counters
    private Long sumOfEntityInMongo;
    private Long sumOfFailedEntityMigration = 0L;

    // Migratior properties
    private ObservableList<String> migrationMessageProperty = FXCollections.observableArrayList();
    private ObservableList<MigrationElement> migrationElements = FXCollections.observableArrayList();
    private LongProperty sumOfAllEntity = new SimpleLongProperty(0);
    private LongProperty sumOfMigratedEntity = new SimpleLongProperty(0);

    // Accept only these entities
    public static final List<ObservationType> PASSED_TYPES = Lists.newArrayList(
            ObservationType.NOTIFICATION_RECORD, ObservationType.WEIGHT_RECORD, ObservationType.BLOOD_GLUCOSE_RECORD,
            ObservationType.BLOOD_PRESSURE_RECORD, ObservationType.PA_LOG_RECORD, ObservationType.MEDICATION_RECORD
    );

    // Savepoints
    private Savepoint beginSP = null;

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

    @Override
    protected Task<Boolean> createTask() {
        return new MigratorTask();
    }

    public ObservableList<MigrationElement> migrationElementProperty() {
        return migrationElements;
    }

    public ObservableList<String> migrationMessageProperty() {
        return migrationMessageProperty;
    }

    public ObservableLongValue sumOfAllEntityProperty() {
        return sumOfAllEntity;
    }

    public ObservableLongValue sumOfMigratedEntityProperty() {
        return sumOfMigratedEntity;
    }

    @Override
    public void reset() {
        super.reset();
        sumOfEntityInMongo = 0L;
        sumOfFailedEntityMigration = 0L;
        migrationMessageProperty.clear();
        sumOfAllEntity = new SimpleLongProperty(0);
        sumOfMigratedEntity = new SimpleLongProperty(0);
        migrationElements.clear();
    }

    private void addNewMigrationElement(NamedEnum type, boolean success, Throwable ex) {
        if(BooleanUtils.toBoolean(Configuration.get("migration.show.entities"))) {
            migrationElements.add(new MigrationElement(new Date(), type, success, ex));
        }
    }

    private void addNewMigrationMessage(String message) {
        if(BooleanUtils.toBoolean(Configuration.get("migration.show.exceptions"))) {
            migrationMessageProperty.add(message);
        }
    }

    public class MigratorTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            return startProcess();
        }

        private boolean startProcess() {
            migrationElements.clear();
            migrationMessageProperty.clear();

            migrationMessageProperty.add("A migrálási folyamat elkezdődött");
            updateProgress(0L, 1L);
            sumOfEntityInMongo = calculateMigrationsProcess();
            migrationMessageProperty.add("MongoDB-ben talált dokumentumok száma: " + sumOfEntityInMongo);
            sumOfAllEntity.setValue( sumOfEntityInMongo );

            try {
                PostgresConnection.getInstance().setAutoCommit(false);
                beginSP = PostgresConnection.getInstance().setSavepoint("BEGIN_ALL_TRANSACTION");

                List<User> users = mongoUserDAO.getAllUser();
                for(User user : users) {
                    if( !isCancelled() ) {
                        migrateUsers(user);
                        LOGGER.info(user.getMongoId() + " id-vel rendelkező felhasználó adatai sikeresen migrálva lettek.");
                    } else {
                        migrationMessageProperty.add("A migrálási folyamatot megszakították.");
                        LOGGER.info("A migrálási folyamatot megszakították.");
                        break;
                    }
                }
            } catch (Exception ex) {
                // connection or setAutoCommit failure
                migrationMessageProperty.add("Nem sikerült beállítani a JDBC tranzakció kezelését");
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

        private void migrateUsers(User user) throws SQLException {
            try {
                User persisted = postgresUserDAO.saveEntity(user);
                migrateDevices(persisted);
                PostgresConnection.getInstance().commit();

                Platform.runLater(() -> updateProgress(getProgress() + 1 , sumOfEntityInMongo));
                addNewMigrationElement(MigrationElement.EntityType.USER, true, null);
                sumOfMigratedEntity.setValue( sumOfMigratedEntity.get() + 1 );
            } catch (SQLException ex) {
                PostgresConnection.getInstance().rollback();
                ex.printStackTrace();
                sumOfFailedEntityMigration++;

                addNewMigrationMessage( ex.getClass().getName() );
                addNewMigrationElement(MigrationElement.EntityType.USER, false, ex);
            } catch (Exception ex) {
                PostgresConnection.getInstance().rollback();
                sumOfFailedEntityMigration++;
            }
        }

        private Long calculateMigrationsProcess() {
            return mongoDeviceDAO.count() + mongoObservationDAO.count() + mongoUserDAO.count();
        }

        private void migrateDevices(User owner) throws Exception {
            List<Device> devices = mongoDeviceDAO.getDeviceByUser(owner);
            for(Device device : devices) {
                try {
                    Device persistedDevice = postgresDeviceDAO.saveEntity(device);
                    Episode persistedEpisode = migrateEpisodes(owner, persistedDevice);
                    postgresMatchingTableDAO.insertToEpisodeDevice(persistedEpisode, persistedDevice);
                    migrateObservations(persistedEpisode, persistedDevice);

                    PostgresConnection.getInstance().commit();
                    addNewMigrationElement(MigrationElement.EntityType.DEVICE, true, null);
                    sumOfMigratedEntity.setValue( sumOfMigratedEntity.get() + 1 );
                } catch (Exception ex) {
                    addNewMigrationMessage( ex.getClass().getName() );
                    addNewMigrationElement(MigrationElement.EntityType.DEVICE, false, ex);
                    throw new Exception(ex);
                }
            }

            // TODO: 2016.09.23. if devices is empty -> expert user
        }

        private Episode migrateEpisodes(User user, Device device) throws SQLException {
            Episode episode = new Episode(user, mongoObservationDAO.getLatestObservationDateByDevice(device));
            return postgresEpisodeDAO.saveEntity(episode);
        }

        private void migrateObservations(Episode episode, Device device) throws Exception {
            List<Observation> observations = mongoObservationDAO.getObservationsByDevice(device);
            observations.forEach(item -> item.setEpisode(episode));

            for(Observation observation : observations) {
                try {
                    postgresEpEventDAO.saveEntity(observation);
                    addNewMigrationElement(observation.getType(), true, null);
                    sumOfMigratedEntity.setValue( sumOfMigratedEntity.get() + 1 );
                } catch (Exception ex) {
                    addNewMigrationElement(observation.getType(), true, null);
                    addNewMigrationMessage( ex.getClass().getName() );
                    throw new Exception(ex);
                }
            }
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            migrationMessageProperty.add("A migrálási folyamat külső hiba nélkül sikeresen végetért!");
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            migrationMessageProperty.add("A migrálási folyamatot megszakították!");
        }

        @Override
        protected void failed() {
            super.failed();
            migrationMessageProperty.add("A migrálási folyamat külső hiba miatt végetért!");
        }
    }

}
