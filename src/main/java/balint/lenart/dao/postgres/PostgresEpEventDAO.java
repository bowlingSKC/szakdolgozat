package balint.lenart.dao.postgres;

import balint.lenart.Configuration;
import balint.lenart.model.observations.*;
import balint.lenart.model.observations.helper.EventAnamnesisIllness;
import balint.lenart.model.observations.helper.EventItemContent;

import java.sql.*;

public class PostgresEpEventDAO {

    private String getSchemaName() {
        return Configuration.get("postgres.connection.schema");
    }

    public Long count() throws SQLException {
        Statement statement = PostgresConnection.getInstance().getConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM log.ep_event");
        result.next();
        return result.getLong(1);
    }

    public Observation saveEntity(Observation observation) throws SQLException {
        observation = saveSuperclass(observation);
        saveKnownEntity(observation);
        return observation;
    }

    private Observation saveSuperclass(Observation observation) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".ep_event(episode_id, event_type_code, status_code, ts_specified, ts_recorded, " +
                        "ts_received, ts_updated, ts_deleted, source_device_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS
        );
        statement.setLong(1, observation.getEpisode().getPostgresId());
        statement.setInt(2, 0);     // FIXME: 2016.09.13. replace this const
        statement.setInt(3, 1);     // FIXME: 2016.09.19. replace this const
        statement.setDate(4, new Date(observation.getTsSpecified().getTime()));
        statement.setDate(5, new Date(observation.getTsRecorded().getTime()));
        statement.setDate(6, new Date(observation.getTsReceived().getTime()));
        if( observation.getTsUpdated() != null ) {
            statement.setDate(7, new Date(observation.getTsUpdated().getTime()));
        } else {
            statement.setDate(7, null);
        }
        if( observation.getTsDeleted() != null ) {
            statement.setDate(8, new Date(observation.getTsDeleted().getTime()));
        } else {
            statement.setDate(8, null);
        }
        statement.setLong(9, observation.getSourceDevice().getPostgresId());
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if( generatedKeys.next() ) {
            observation.setPostgresId( generatedKeys.getLong(1) );
        }
        return observation;
    }

    private void saveKnownEntity(Observation observation) throws SQLException {
        if( observation instanceof MissingFood ) {
            saveMissingFoodEntity((MissingFood) observation);
        } else if( observation instanceof PhysicalEvent) {
            savePhysicalEvent((PhysicalEvent) observation);
        } else if( observation instanceof BloodPressureMeas) {
            saveBloodPressureEvent((BloodPressureMeas) observation);
        } else if( observation instanceof GlucoseMeas) {
            saveGlucoseMeas((GlucoseMeas) observation);
        } else if( observation instanceof WeightMeas ) {
            saveWeightMeas((WeightMeas) observation);
        } else if( observation instanceof Medication ) {
            saveMedicationEvent((Medication) observation);
        } else if( observation instanceof Meal ) {
            saveMealEvent((Meal)observation);
        } else if( observation instanceof Anamnesis ) {
            saveAnamnesis((Anamnesis)observation);
        }
    }

    private void saveAnamnesis(Anamnesis observation) throws SQLException {
        PreparedStatement insertStatement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_anamnesis(event_id, height, weight, birth_date, gender_code, " +
                        "lifestyle_code, mass_change, mass_change_time, egfr, steorid_treatment, insulin_dose) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
        );
        insertStatement.setLong(1, observation.getPostgresId());
        if( observation.getHeight() != null ) {
            insertStatement.setInt(2, observation.getHeight());
        } else {
            insertStatement.setNull(2, Types.INTEGER);
        }

        if( observation.getWeight() != null ) {
            insertStatement.setDouble(3, observation.getWeight());
        } else {
            insertStatement.setNull(3, Types.REAL);
        }

        if( observation.getBirthDate() != null ) {
            insertStatement.setDate(4, new Date(observation.getBirthDate().getTime()));
        } else {
            insertStatement.setNull(4, Types.DATE);
        }

        if( observation.getGenderCode() != null ) {
            insertStatement.setInt(5, observation.getGenderCode());
        } else {
            insertStatement.setNull(5, Types.INTEGER);
        }

        if( observation.getLifestyleCode() != null ) {
            insertStatement.setInt(6, observation.getLifestyleCode());
        } else {
            insertStatement.setNull(6, Types.INTEGER);
        }

        if( observation.getMassChange() != null ) {
            insertStatement.setDouble(7, observation.getMassChange());
        } else {
            insertStatement.setNull(7, Types.REAL);
        }

        if( observation.getMassChangeTime() != null ) {
            insertStatement.setInt(8, observation.getMassChangeTime());
        } else {
            insertStatement.setNull(8, Types.INTEGER);
        }

        if( observation.getEgfr() != null ) {
            insertStatement.setDouble(9, observation.getEgfr());
        } else {
            insertStatement.setNull(9, Types.REAL);
        }

        if( observation.getSteroidTreatment() != null ) {
            insertStatement.setBoolean(10, observation.getSteroidTreatment());
        } else {
            insertStatement.setNull(10, Types.BOOLEAN);
        }

        if( observation.getInsulinDose() != null ) {
            insertStatement.setDouble(11, observation.getInsulinDose());
        } else {
            insertStatement.setNull(11, Types.REAL);
        }
        insertStatement.execute();
        saveAnamnesisIllnesses(observation);
    }

    private void saveAnamnesisIllnesses(Anamnesis anamnesis) throws SQLException {
        for (EventAnamnesisIllness illness : anamnesis.getIllnesses()) {
            PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                    "INSERT INTO " + getSchemaName() + ".event_anamnesis_illness(anamnesis_id, illness_id) VALUES (?, ?)"
            );
            statement.setLong(1, anamnesis.getPostgresId());
            statement.setLong(2, illness.getIllnessId());
            statement.execute();
        }
    }

    private void saveMealEvent(Meal observation) throws SQLException {
        PreparedStatement insertMealStatement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_meal(event_id, ts_meal_end, meal_type_code, glyc_load) " +
                        "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
        );
        insertMealStatement.setLong(1, observation.getPostgresId());
        insertMealStatement.setDate(2, new Date(new java.util.Date().getTime()));
        insertMealStatement.setInt(3, observation.getMealTypeCode());
        insertMealStatement.setNull(4, Types.FLOAT);
        insertMealStatement.execute();

        for(MealItem mealItem : observation.getMealItems()) {
            MealItem savedSuperMealItem = (MealItem) saveSuperclass(mealItem);
            PreparedStatement insertMealItem = PostgresConnection.getInstance().getConnection().prepareStatement(
                    "INSERT INTO " + getSchemaName() + ".event_mealitem(event_id, item_type_code, item_label, meal_id, quantity, " +
                            "unit_id, unit_label) VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );
            insertMealItem.setLong(1, savedSuperMealItem.getPostgresId());
            insertMealItem.setInt(2, mealItem.getItemTypeCode());
            insertMealItem.setString(3, mealItem.getItemLabel());
            insertMealItem.setLong(4, mealItem.getMeal().getPostgresId());
            insertMealItem.setDouble(5, mealItem.getQuantity());
            insertMealItem.setLong(6, mealItem.getUnitId());
            insertMealItem.setString(7, mealItem.getUnitLabel());
            insertMealItem.execute();

            ResultSet mealItemId = insertMealItem.getGeneratedKeys();
            if( mealItemId.next() ) {
                long mealItemIdResult = mealItemId.getLong(1);
                for (EventItemContent eventItemContent : mealItem.getItemContents()) {
                    PreparedStatement insertEventItem = PostgresConnection.getInstance().getConnection().prepareStatement(
                            "INSERT INTO " + getSchemaName() + ".event_item_content(item_id, nutr_id, quantity) VALUES (?, ?, ?)"
                    );
                    insertEventItem.setLong(1, mealItemIdResult);
                    insertEventItem.setLong(2, eventItemContent.getNutrientId());
                    insertEventItem.setDouble(3, eventItemContent.getQuantity());
                    insertEventItem.execute();
                }
            }
        }
    }

    private void saveMedicationEvent(Medication medication) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_medication(event_id, medication_id, quantity, unit_id, unit_label, " +
                        "admin_route_code, admin_loc_code, related_meal_id, meal_relation_type_code, related_meal_type_code) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
        );
        statement.setLong(1, medication.getPostgresId());
        statement.setInt(2, medication.getMedicationId());
        statement.setDouble(3, medication.getQuantity());
        statement.setInt(4, medication.getUnitId());
        statement.setString(5, medication.getUnitLabel());
        if( medication.getAdminRouteCode() == null ) {
            statement.setNull(6, Types.INTEGER);
        } else {
            statement.setInt(6, medication.getAdminRouteCode());
        }
        if( medication.getAdminLocCode() == null ) {
            statement.setNull(7, Types.INTEGER);
        } else {
            statement.setInt(7, medication.getAdminLocCode());
        }
        statement.setNull(8, Types.INTEGER);
        if( medication.getMealRelatedTypeCode() == null ) {
            statement.setNull(9, Types.INTEGER);
        } else {
            statement.setInt(9, medication.getMealRelatedTypeCode());
        }
        if( medication.getRelatedMealTypeCode() == null ) {
            statement.setNull(10, Types.INTEGER);
        } else {
            statement.setInt(10, medication.getRelatedMealTypeCode());
        }
        statement.execute();
    }

    private void saveWeightMeas(WeightMeas observation) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_weight_meas(event_id, weight_data) VALUES (?, ?);"
        );
        statement.setLong(1, observation.getPostgresId());
        statement.setDouble(2, observation.getWeightData());
        statement.execute();
    }

    private void saveGlucoseMeas(GlucoseMeas observation) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_glucose_meas(event_id, meas_time_code, glucose_data) " +
                        "VALUES (?, ?, ?);"
        );
        statement.setLong(1, observation.getPostgresId());
        statement.setNull(2, Types.INTEGER);
        statement.setDouble(3, observation.getGlucoseData());
        statement.execute();
    }

    private void saveBloodPressureEvent(BloodPressureMeas observation) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_bp_meas(event_id, systolic_data, diastolic_data, pulse_data) " +
                        "VALUES (?, ?, ?, ?);"
        );
        statement.setLong(1, observation.getPostgresId());
        statement.setInt(2, observation.getSystolicData());
        statement.setInt(3, observation.getDiastolicData());
        statement.setInt(4, observation.getPulseData());
        statement.execute();
    }

    private void savePhysicalEvent(PhysicalEvent physicalEvent) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_physical(event_id, pa_id, pa_label, duration, energy_consumed) " +
                        "VALUES (?, ?, ?, ?, ?);");
        statement.setLong(1, physicalEvent.getPostgresId());
        statement.setInt(2, physicalEvent.getPaId());
        statement.setString(3, physicalEvent.getPaLabel());
        if( physicalEvent.getDuration() == null ) {
            statement.setNull(4, Types.DOUBLE);
        } else {
            statement.setInt(4, physicalEvent.getDuration());
        }
        statement.setInt(5, physicalEvent.getEnergyConsumed());
        statement.execute();
    }

    private void saveMissingFoodEntity(MissingFood entity) throws SQLException {
        PreparedStatement statement = PostgresConnection.getInstance().getConnection().prepareStatement(
                "INSERT INTO " + getSchemaName() + ".event_missing_food(event_id, food_id, recipe_id, message_text) " +
                        "VALUES (?, ?, ?, ?);");
        statement.setLong(1, entity.getPostgresId());
        if( entity.getFoodId() == null ) {
            statement.setNull(2, Types.INTEGER);
        } else {
            statement.setInt(2, entity.getFoodId());
        }

        if( entity.getRecipeId() == null ) {
            statement.setNull(3, Types.INTEGER);
        } else {
            statement.setInt(3, entity.getRecipeId());
        }
        statement.setString(4, entity.getMessageText());
        statement.execute();
    }

}
