package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class PhysicalEvent extends Observation {

    private int paId;
    private String paLabel;
    private Integer duration;
    private Integer energyConsumed;

    public PhysicalEvent() {
    }

    public PhysicalEvent(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated,
                         Date tsDeleted, Device sourceDevice, int paId, String paLabel, Integer duration,
                         Integer energyConsumed) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.paId = paId;
        this.paLabel = paLabel;
        this.duration = duration;
        this.energyConsumed = energyConsumed;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.PA_LOG_RECORD;
    }

    public int getPaId() {
        return paId;
    }

    public void setPaId(int paId) {
        this.paId = paId;
    }

    public String getPaLabel() {
        return paLabel;
    }

    public void setPaLabel(String paLabel) {
        this.paLabel = paLabel;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getEnergyConsumed() {
        return energyConsumed;
    }

    public void setEnergyConsumed(Integer energyConsumed) {
        this.energyConsumed = energyConsumed;
    }
}
