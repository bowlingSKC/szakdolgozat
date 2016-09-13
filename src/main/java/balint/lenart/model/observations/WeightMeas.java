package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class WeightMeas extends Observation {

    private Double weightData;

    public WeightMeas() {
    }

    public WeightMeas(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated,
                      Date tsDeleted, Device sourceDevice, Double weightData) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.weightData = weightData;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.WEIGHT_RECORD;
    }

    public Double getWeightData() {
        return weightData;
    }

    public void setWeightData(Double weightData) {
        this.weightData = weightData;
    }
}
