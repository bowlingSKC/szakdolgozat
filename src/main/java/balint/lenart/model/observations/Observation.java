package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class Observation {

    protected Long postgresId;
    protected Episode episode;
    protected int eventTypeCode;
    protected int statusCode = 0;
    protected Date tsSpecified;
    protected Date tsRecorded;
    protected Date tsReceived;
    protected Date tsUpdated;
    protected Date tsDeleted;
    protected Device sourceDevice;

    public abstract ObservationType getType();

}