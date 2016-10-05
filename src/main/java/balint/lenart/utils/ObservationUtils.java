package balint.lenart.utils;

import balint.lenart.model.observations.*;
import balint.lenart.model.observations.helper.EventItemContent;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.bson.Document;

public class ObservationUtils {

    private static final String TYPE_KEY = "type";

    public static Observation fillByDocument(Document document) {
        Observation observation = null;
        switch (getObservationTypeByDocument(document)) {
            case NOTIFICATION_RECORD:
                observation = createMissingFoodObservation(document);
                break;
            case WEIGHT_RECORD:
                observation = createWeightRecord(document);
                break;
            case BLOOD_GLUCOSE_RECORD:
                observation = createGlucoseMeas(document);
                break;
            case BLOOD_PRESSURE_RECORD:
                observation = createBloodPressureMeas(document);
                break;
            case PA_LOG_RECORD:
                observation = createPhysicalEvent(document);
                break;
            case MEDICATION_RECORD:
                observation = createMedicationEvent(document);
                break;
            case MEAL_LOG_RECORD:
                observation = createMealLogRecord(document);
                break;
        }

        observation.setTsReceived( document.getDate("timestampIn") );
        observation.setTsRecorded( document.getDate("timestampIn") );
        observation.setTsSpecified( document.getDate("timestampRecorded") );
        // check softdeleted
        if(BooleanUtils.isTrue( document.getBoolean("deleted") )) {
            observation.setTsDeleted( document.getDate("timestampUpdated") );
        }

        return observation;
    }

    public static ObservationType getObservationTypeByDocument(Document document) {
        String documentType = document.getString( TYPE_KEY );
        ObservationType[] observationTypes = ObservationType.values();
        for(ObservationType type : observationTypes) {
            if( type.getClassName().equals(documentType) ) {
                return type;
            }
        }
        throw new RuntimeException("Unhandled observation type: " + documentType);
    }

    private static Observation createMealLogRecord(Document document) {
        Document contentDoc = document.get("content", Document.class);
        Meal meal = new Meal();
        //meal.setTsMealEnd( document.get("content", Document.class).getDate("timestamp") );

        MealItem mealItem = new MealItem();
        mealItem.setMeal( meal );
        mealItem.setQuantity( contentDoc.containsKey("quantity") ? contentDoc.getDouble("quantity").floatValue() : 1 );
        mealItem.setItemLabel( contentDoc.getString("label") );
        mealItem.setUnitId( contentDoc.getInteger("itemId") );
        mealItem.setUnitLabel( contentDoc.getString("unitLabel") );

        Document itemContentDoc = contentDoc.get("content", Document.class);
        if( itemContentDoc != null ) {
            itemContentDoc.forEach((key, value) ->
                    mealItem.getItemContents().add(new EventItemContent(Integer.valueOf(key), ((Double)value).floatValue())));
        }

        meal.setMealItems(Lists.newArrayList(mealItem));
        mealItem.setItemTypeCode( 0 );    // FIXME: 2016.10.05. replace from const
        return meal;
    }

    private static Observation createMedicationEvent(Document document) {
        Medication event = new Medication();
        event.setMedicationId( document.get("content", Document.class).getInteger("medicationId") );
        event.setQuantity( document.get("content", Document.class).getDouble("quantity") );
        event.setUnitId( document.get("content", Document.class).getInteger("unitId") );
        event.setUnitLabel( document.get("content", Document.class).getString("unitLabel") );
        if( document.containsKey("context") ) {
            event.setAdminRouteCode( CodeUtils.getMedicationAdminRouteCode(document.get("context", Document.class).getString("routeOfAdministration")) );
            event.setAdminLocCode( document.get("context", Document.class).getInteger("placeOfAdministration") );
            event.setMealRelatedTypeCode(CodeUtils.getMedicationTimeOfAdministration(document.get("context", Document.class).getInteger("timeOfAdministration")));
            event.setRelatedMealTypeCode( document.get("context", Document.class).getInteger("relatedTo") );
        } // TODO: 2016.09.26. can be contextType
        return event;
    }

    private static PhysicalEvent createPhysicalEvent(Document document) {
        PhysicalEvent event = new PhysicalEvent();
        event.setPaId( document.get("content", Document.class).getInteger("itemId") );
        int energyConsumed = 0;
        Document mapContent = document.get("content", Document.class).get("content", Document.class);
        for(String key : mapContent.keySet()) {
            energyConsumed += mapContent.getDouble(key);
        }
        event.setEnergyConsumed(energyConsumed);
        return event;
    }

    private static BloodPressureMeas createBloodPressureMeas(Document document) {
        BloodPressureMeas event = new BloodPressureMeas();
        event.setDiastolicData( document.get("content", Document.class).getInteger("diastolic") );
        event.setSystolicData( document.get("content", Document.class).getInteger("systolic") );
        event.setPulseData( document.get("content", Document.class).getInteger("pulse") );
        return event;
    }

    private static GlucoseMeas createGlucoseMeas(Document document) {
        GlucoseMeas event = new GlucoseMeas();
        event.setGlucoseData( document.get("content", Document.class).getDouble("bloodGlucose") );
        return event;
    }

    private static WeightMeas createWeightRecord(Document document) {
        WeightMeas event = new WeightMeas();
        event.setWeightData( document.get("content", Document.class).getDouble("weight") );
        return event;
    }

    private static MissingFood createMissingFoodObservation(Document document) {
        MissingFood event = new MissingFood();
        // food (type = 1) or recipe (type = 2), nospec if type = 0
        if( document.get("content", Document.class).getInteger("type") == 1 ) {
            event.setFoodId( document.get("content", Document.class).getInteger("extraInt") );
        } else if( document.get("content", Document.class).getInteger("type") == 2 ) {
            event.setRecipeId( document.get("content", Document.class).getInteger("extraInt") );
        }
        event.setMessageText( document.get("content", Document.class).getString("extraString") );
        return event;
    }

}
