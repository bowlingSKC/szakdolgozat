package balint.lenart.services;

import balint.lenart.Configuration;
import balint.lenart.MigrationSettingsLevel;
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
import balint.lenart.utils.FXUtils;
import com.google.common.collect.Lists;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Date;
import java.util.List;

public class Migrator extends Service<Boolean> {

    private static Migrator instance;
    private static final Logger LOGGER = Logger.getLogger("MigrationLogger");

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
    private LongProperty sumOfEntityInMongo = new SimpleLongProperty(0L);           // all entity count in MongoDB
    private LongProperty sumOfFailedEntityMigration = new SimpleLongProperty(0L);   // failed entity number
    private LongProperty sumOfMigratedEntityCounter = new SimpleLongProperty(0L);   // success migration entity number

    // Migratior properties
    private ObservableList<String> migrationMessageProperty = FXCollections.observableArrayList();
    private ObservableList<MigrationElement> migrationElements = FXCollections.observableArrayList();

    // Accept only these entities
    public static final List<ObservationType> PASSED_TYPES = Lists.newArrayList(
            ObservationType.NOTIFICATION_RECORD, ObservationType.WEIGHT_RECORD, ObservationType.BLOOD_GLUCOSE_RECORD,
            ObservationType.BLOOD_PRESSURE_RECORD, ObservationType.PA_LOG_RECORD, ObservationType.MEDICATION_RECORD,
            ObservationType.MEAL_LOG_RECORD
    );

    // Savepoints
    private Savepoint lastDeviceSP = null;
    private Savepoint lastObservationSP = null;

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
            LOGGER.trace("A migrátori folyamat sikeresen inicializálódott");
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

    @Override
    public void reset() {
        super.reset();
        sumOfEntityInMongo.setValue(0L);
        sumOfFailedEntityMigration.setValue(0L);
        sumOfMigratedEntityCounter.setValue(0L);
        migrationMessageProperty.clear();
        migrationElements.clear();
    }

    private void addNewMigrationElement(NamedEnum type, boolean success, Throwable ex) {
        if(Configuration.getBoolean("migration.show.entities")) {
            migrationElements.add(new MigrationElement(new Date(), type, success, ex));
        }
    }

    private void addNewMigrationMessage(String message) {
        if(Configuration.getBoolean("migration.show.exceptions")) {
            migrationMessageProperty.add(message);
        }
    }

    public LongProperty sumOfEntityInMongoProperty() {
        return sumOfEntityInMongo;
    }

    public LongProperty sumOfFailedEntityMigrationProperty() {
        return sumOfFailedEntityMigration;
    }

    public LongProperty sumOfMigratedEntityCounterProperty() {
        return sumOfMigratedEntityCounter;
    }

    public class MigratorTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            return startProcess();
        }

        private void updateProgress() {
            sumOfMigratedEntityCounter.setValue( sumOfMigratedEntityCounter.get() + 1 );
            FXUtils.runInFxThread(() -> updateProgress(sumOfMigratedEntityCounter.doubleValue(), sumOfEntityInMongo.doubleValue()));
        }

        private boolean startProcess() {
            migrationElements.clear();
            migrationMessageProperty.clear();

            migrationMessageProperty.add("A migrálási folyamat elkezdődött");
            sumOfEntityInMongo.setValue(calculateMigrationsProcess());
            migrationMessageProperty.add("MongoDB-ben talált dokumentumok száma: " + sumOfEntityInMongo.get());
            updateProgress();

            LOGGER.info("A migrálási folyamat elkezdődött");
            LOGGER.info("A migrációs folyamat tranzakciós szintje: " + Configuration.getMigrationLevel().getName());
            LOGGER.info("MongoDB-ben talált dokumentumok száma: " + sumOfEntityInMongo.get());

            try {
                PostgresConnection.getInstance().setAutoCommit(false);
                LOGGER.trace("JDBC tranzakació szintje: manuális");

                List<User> users = mongoUserDAO.getAllUser();
                for(User user : users) {
                    if( !isCancelled() ) {
                        migrateUsers(user);
                    } else {
                        migrationMessageProperty.add("A migrálási folyamatot megszakították.");
                        LOGGER.info("A migrálási folyamatot megszakították.");
                        break;
                    }
                }
            } catch (Exception ex) {
                // connection or setAutoCommit failure
                migrationMessageProperty.add("Nem sikerült beállítani a JDBC tranzakció kezelését");
                LOGGER.error("Nem sikerült beállítani a JDBC tranzakció kezelését");
                ex.printStackTrace();
            } finally {
                try {
                    PostgresConnection.getInstance().setAutoCommit(true);
                    LOGGER.trace("JDBC tranzakació szintje: auto");
                } catch (SQLException e) {
                    // ignore this exception
                    LOGGER.warn("Nem sikerült a JDBC tranzakció szintjét visszaállítani");
                }
            }

            return sumOfFailedEntityMigration.equals(0L);
        }

        private void migrateUsers(User user) throws SQLException {
            try {
                User persisted = postgresUserDAO.saveEntity(user);
                updateProgress();

                migrateDevices(persisted);
                PostgresConnection.getInstance().commit();

                updateProgress();
                addNewMigrationElement(MigrationElement.EntityType.USER, true, null);
                LOGGER.trace("Felhasználó sikeresen migrálva, MongoID: " + user.getMongoId() + ", PostgresID: " + user.getPostgresId());
            } catch (SQLException ex) {
                PostgresConnection.getInstance().rollback();
                ex.printStackTrace();
                sumOfFailedEntityMigration.setValue( sumOfFailedEntityMigration.get() + 1 );

                LOGGER.warn("Hiba történt egy felhasználó migrálásakkor! MongoID: " + user.getMongoId(), ex);
                addNewMigrationMessage( ex.getClass().getName() );
                addNewMigrationElement(MigrationElement.EntityType.USER, false, ex);
            } catch (Exception ex) {
                PostgresConnection.getInstance().rollback();

                LOGGER.warn("Hiba történt egy felhasználó migrálásakkor! MongoID: " + user.getMongoId(), ex);
                sumOfFailedEntityMigration.setValue( sumOfFailedEntityMigration.get() + 1 );
            }
        }

        private Long calculateMigrationsProcess() {
            return mongoDeviceDAO.count() + mongoObservationDAO.count() + mongoUserDAO.count();
        }

        private void migrateDevices(User owner) throws Exception {
            List<Device> devices = mongoDeviceDAO.getDeviceByUser(owner);
            for(Device device : devices) {
                try {
                    lastDeviceSP = PostgresConnection.getInstance().setSavepoint();
                    Device persistedDevice = postgresDeviceDAO.saveEntity(device);
                    Episode persistedEpisode = migrateEpisodes(owner, persistedDevice);
                    postgresMatchingTableDAO.insertToEpisodeDevice(persistedEpisode, persistedDevice);

                    migrateObservations(persistedEpisode, persistedDevice);

                    updateProgress();
                    LOGGER.trace("Eszköz sikeresen migrálva, MongoID: " + device.getMongoId() + ", PostgresID: " + device.getPostgresId());
                    addNewMigrationElement(MigrationElement.EntityType.DEVICE, true, null);
                } catch (Exception ex) {
                    addNewMigrationMessage( ex.getClass().getName() );
                    addNewMigrationElement(MigrationElement.EntityType.DEVICE, false, ex);
                    LOGGER.warn("Hiba történt egy eszköz migrálásakkor! MongoID: " + device.getMongoId(), ex);
                    sumOfFailedEntityMigration.setValue( sumOfFailedEntityMigration.get() + 1 );
                    if( Configuration.getMigrationLevel().equals(MigrationSettingsLevel.DEVICE) ) {
                        PostgresConnection.getInstance().rollback(lastDeviceSP);
                    } else {
                        throw ex;
                    }
                }
            }
        }

        private Episode migrateEpisodes(User user, Device device) throws SQLException {
            Episode episode = new Episode(user, mongoObservationDAO.getLatestObservationDateByDevice(device));
            return postgresEpisodeDAO.saveEntity(episode);
        }

        private boolean isTypeEnabled(ObservationType observationType) {
            switch (observationType) {
                case BLOOD_GLUCOSE_RECORD:
                    return Configuration.getBoolean("migration.items.bloodglucose");
                case BLOOD_PRESSURE_RECORD:
                    return Configuration.getBoolean("migration.items.bloodpressure");
                case CHGI_LOG_RECORD:
                    return Configuration.getBoolean("migration.items.chgi");
                case COMMENT_RECORD:
                    return Configuration.getBoolean("migration.items.comment");
                case DIETLOG_ANAM_RECORD:
                    return Configuration.getBoolean("migration.items.dietlog");
                case LAB_RECORD:
                    return Configuration.getBoolean("migration.items.lab");
                case MEAL_LOG_RECORD:
                    return Configuration.getBoolean("migration.items.meal");
                case MEDICATION_RECORD:
                    return Configuration.getBoolean("migration.items.medication");
                case NOTIFICATION_RECORD:
                    return Configuration.getBoolean("migration.items.missingfood");
                case PA_LOG_RECORD:
                    return Configuration.getBoolean("migration.items.pa");
                case WEIGHT_RECORD:
                    return Configuration.getBoolean("migration.items.weight");
                default:
                    throw new RuntimeException("Unsupported observation type!");
            }
        }

        private void migrateObservations(Episode episode, Device device) throws Exception {
            for (ObservationType type : PASSED_TYPES) {
                if( isTypeEnabled(type) ) {
                    List<Observation> observationsByType = mongoObservationDAO.getObservationsByDeviceAndType(device, type);
                    observationsByType.forEach(item -> item.setEpisode(episode));
                    observationsByType.forEach(item -> item.setSourceDevice(device));

                    for (Observation observation : observationsByType) {
                        migrateObservation(observation);
                    }
                }
            }
        }

        private void migrateObservation(Observation observation) throws Exception {
            try {
                lastObservationSP = PostgresConnection.getInstance().setSavepoint();
                postgresEpEventDAO.saveEntity(observation);

                updateProgress();
                LOGGER.trace("Megfigyelés sikeresen migrálva, PostgresID: " + observation.getPostgresId());
                addNewMigrationElement(observation.getType(), true, null);
            } catch (Exception ex) {
                LOGGER.warn("Hiba történt egy megfigyelés migrálásakkor! MongoID: " + observation.getPostgresId(), ex);
                addNewMigrationElement(observation.getType(), false, ex);
                addNewMigrationMessage( ex.getClass().getName() );
                sumOfFailedEntityMigration.setValue( sumOfFailedEntityMigration.get() + 1 );
                if( Configuration.getMigrationLevel().equals(MigrationSettingsLevel.OBSERVATION) ) {
                    PostgresConnection.getInstance().rollback(lastObservationSP);
                } else {
                    throw ex;
                }
            }
        }

        private void beforeFinish() {
            migrationMessageProperty.add("Sikeresen migrált entitások száma: " + sumOfMigratedEntityCounter.get());
            migrationMessageProperty.add("Sikertelenül migrált entitások száma: " + sumOfFailedEntityMigration.get());

        }

        @Override
        protected void succeeded() {
            beforeFinish();
            super.succeeded();
            reset();
            migrationMessageProperty.add("A migrálási folyamat külső hiba nélkül sikeresen végetért!");
            LOGGER.info("A migrációs folyamat külső hiba nélkül sikeresen végetért!");
        }

        @Override
        protected void cancelled() {
            beforeFinish();
            super.cancelled();
            reset();
            migrationMessageProperty.add("A migrálási folyamatot megszakították!");
            LOGGER.info("A migrációs folyamatot megszakítoták!");
        }

        @Override
        protected void failed() {
            beforeFinish();
            super.failed();
            reset();
            migrationMessageProperty.add("A migrálási folyamat külső hiba miatt végetért!");
            LOGGER.error("A migrálási folyamat külső hiba miatt végetért!");
        }
    }

}
