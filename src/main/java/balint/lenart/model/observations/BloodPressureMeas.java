package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class BloodPressureMeas extends Observation {

    private Integer systolicData;
    private Integer diastolicData;
    private Integer pulseData;

    public BloodPressureMeas() {
    }

    public BloodPressureMeas(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived,
                             Date tsUpdated, Date tsDeleted, Device sourceDevice, Integer systolicData,
                             Integer diastolicData, Integer pulseData) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.systolicData = systolicData;
        this.diastolicData = diastolicData;
        this.pulseData = pulseData;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.BLOOD_PRESSURE_RECORD;
    }

    public Integer getSystolicData() {
        return systolicData;
    }

    public void setSystolicData(Integer systolicData) {
        this.systolicData = systolicData;
    }

    public Integer getDiastolicData() {
        return diastolicData;
    }

    public void setDiastolicData(Integer diastolicData) {
        this.diastolicData = diastolicData;
    }

    public Integer getPulseData() {
        return pulseData;
    }

    public void setPulseData(Integer pulseData) {
        this.pulseData = pulseData;
    }
}
