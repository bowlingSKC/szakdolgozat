package balint.lenart.utils;

import balint.lenart.model.observations.*;
import balint.lenart.model.observations.helper.EventAnamnesisIllness;
import balint.lenart.model.observations.helper.EventItemContent;
import com.google.common.collect.Lists;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.apache.commons.lang3.BooleanUtils;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
            case DIETLOG_ANAM_RECORD:
                observation = createDietAnamRecord(document);
        }
        fillTimestampDates(observation, document);
        return observation;
    }

    private static void fillTimestampDates(Observation observation, Document document) {
        observation.setTsReceived( document.getDate("timestampIn") );
        observation.setTsRecorded( document.getDate("timestampIn") );
        observation.setTsSpecified( document.getDate("timestampRecorded") );
        // check softdeleted
        if(BooleanUtils.isTrue( document.getBoolean("deleted") )) {
            observation.setTsDeleted( document.getDate("timestampUpdated") );
        }
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

    private static Observation createDietAnamRecord(Document document) {
        Anamnesis anamnesis = new Anamnesis();
        Document contentDoc = document.get("content", Document.class);
        anamnesis.setHeight( contentDoc.getInteger("height") );
        anamnesis.setWeight( getDoubleOrInt(contentDoc, "weight").doubleValue() );
        if( contentDoc.containsKey("birthDate") ) {
            anamnesis.setBirthDate( DateUtils.formatDayFormat( contentDoc.getString("birthDate") ) );
        }
        if( contentDoc.getBoolean("isMale") ) {
            anamnesis.setGenderCode(1);
        } else {
            anamnesis.setGenderCode(2);
        }
        anamnesis.setLifestyleCode( contentDoc.getInteger("activity") );
        anamnesis.setSportCode( contentDoc.getInteger("activityTime") );
        anamnesis.setMassChange( contentDoc.getDouble("massDelta") );
        anamnesis.setMassChangeTime( getDoubleOrInt(contentDoc, "massDeltaTime").intValue() );
        fillAnamnesisIllnesses(contentDoc, anamnesis);
        return anamnesis;
    }

    private static Anamnesis fillAnamnesisIllnesses(Document contentDocument, Anamnesis newEntity) {
        List<Integer> illnessesIds = (List<Integer>) contentDocument.get("illnesses");
        if( illnessesIds != null ) {
            illnessesIds.forEach(value -> newEntity.getIllnesses().add(new EventAnamnesisIllness(value.longValue())));
        }
        return newEntity;
    }

    public static Anamnesis fillAnamnesisWithLabRecord(Anamnesis anamnesis, Document document) {
        Document contentDoc = document.get("content", Document.class);
        anamnesis.setEgfr( getDoubleOrInt(contentDoc, "egfr").doubleValue() );
        anamnesis.setInsulinDose(Double.valueOf(contentDoc.getInteger("dailyInsulinDose")));
        anamnesis.setSteroidTreatment( contentDoc.getBoolean("steroidTreatment") );
        return anamnesis;
    }

    private static Observation createMealLogRecord(Document document) {
        Document contentDoc = document.get("content", Document.class);
        Meal meal = new Meal();
        if( contentDoc.containsKey("timestamp") ) {
            meal.setHelperDateTime( contentDoc.getDate("timestamp") );
        } else {
            meal.setHelperDateTime( document.getDate("timestampRecorded") );
        }

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

    public static List<Observation> groupMealItems(FindIterable<Document> mongoMealObservations) {
        List<Observation> observations = Lists.newArrayList();
        List<Meal> helperMealList = Lists.newArrayList();
        mongoMealObservations.forEach((Block<Document>) document -> {
            Date refDate = document.get("content", Document.class).containsKey("timestamp") ?
                    DateUtils.formatMealTimestampDate(document.get("content", Document.class).getString("timestamp")) :
                    document.getDate("timestampRecorded");
            int typeCode = document.get("content", Document.class).getInteger("itemTypeId");
            Optional<Meal> mealOptional = helperMealList
                    .stream()
                    .filter(item -> DateUtils.isSameDay(item.getHelperDateTime(), refDate) &&
                            item.getMealTypeCode() == typeCode)
                    .findFirst();

            Meal selectedMeal = null;
            if( mealOptional.isPresent() ) {
                selectedMeal = mealOptional.get();
            } else {
                selectedMeal = createNewMeal(document);
                helperMealList.add(selectedMeal);
            }

            selectedMeal.getMealItems().add( createNewMealItem(selectedMeal, document) );
        });
        observations.addAll(helperMealList);
        return observations;
    }

    private static MealItem createNewMealItem(Meal selectedMeal, Document document) {
        Document contentDoc = document.get("content", Document.class);
        MealItem mealItem = new MealItem();
        mealItem.setMeal(selectedMeal);
        mealItem.setQuantity( contentDoc.containsKey("quantity") ? getDoubleOrInt(contentDoc, "quantity").floatValue() : 1.0f );
        mealItem.setItemLabel( contentDoc.getString("label") );
        mealItem.setUnitId( contentDoc.getInteger("itemId") );
        mealItem.setUnitLabel( contentDoc.getString("unitLabel") );
        if( contentDoc.containsKey("content") ) {
            fillMealItemWithContent(mealItem, contentDoc.get("content", Document.class));
        }
        fillTimestampDates(mealItem, document);
        return mealItem;
    }

    private static Number getDoubleOrInt(Document doc, String key) {
        try {
            return doc.getDouble(key);
        } catch (ClassCastException ex) {
            return doc.getInteger(key);
        }
    }

    private static void fillMealItemWithContent(MealItem mealItem, Document contentDoc) {
        contentDoc.forEach((key, value) -> mealItem.getItemContents().add(new EventItemContent(Integer.valueOf(key), (double)value)));
    }

    private static Meal createNewMeal(Document document) {
        Meal meal = new Meal();
        Document contentDoc = document.get("content", Document.class);
        meal.setHelperDateTime( contentDoc.containsKey("timestamp") ? DateUtils.formatMealTimestampDate(contentDoc.getString("timestamp")) : document.getDate("timestampRecorded") );
        meal.setMealTypeCode( contentDoc.getInteger("itemTypeId") );
        fillTimestampDates(meal, document);
        return meal;
    }

}
