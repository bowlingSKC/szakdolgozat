package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class Meal extends Observation {

    private Date tsMealEnd;
    private Integer mealTypeCode;
    private Float glycLoad;

    public Meal() {
    }

    public Meal(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated, Date tsDeleted,
                Device sourceDevice, Date tsMealEnd, Integer mealTypeCode, Float glycLoad) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.tsMealEnd = tsMealEnd;
        this.mealTypeCode = mealTypeCode;
        this.glycLoad = glycLoad;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.MEAL_LOG_RECORD;
    }

    public Date getTsMealEnd() {
        return tsMealEnd;
    }

    public void setTsMealEnd(Date tsMealEnd) {
        this.tsMealEnd = tsMealEnd;
    }

    public Integer getMealTypeCode() {
        return mealTypeCode;
    }

    public void setMealTypeCode(Integer mealTypeCode) {
        this.mealTypeCode = mealTypeCode;
    }

    public Float getGlycLoad() {
        return glycLoad;
    }

    public void setGlycLoad(Float glycLoad) {
        this.glycLoad = glycLoad;
    }
}
