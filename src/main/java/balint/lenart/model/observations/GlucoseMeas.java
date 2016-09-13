package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class GlucoseMeas extends Observation {

    private Integer measTimeCode;
    private Double glucoseData;

    public GlucoseMeas() {
    }

    public GlucoseMeas(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated,
                       Date tsDeleted, Device sourceDevice, Integer measTimeCode, Double glucoseData) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.measTimeCode = measTimeCode;
        this.glucoseData = glucoseData;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.BLOOD_GLUCOSE_RECORD;
    }

    public Integer getMeasTimeCode() {
        return measTimeCode;
    }

    public void setMeasTimeCode(Integer measTimeCode) {
        this.measTimeCode = measTimeCode;
    }

    public Double getGlucoseData() {
        return glucoseData;
    }

    public void setGlucoseData(Double glucoseData) {
        this.glucoseData = glucoseData;
    }
}
