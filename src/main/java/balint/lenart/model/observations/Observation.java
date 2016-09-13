package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public abstract class Observation {

    protected Long postgresId;
    protected Episode episode;
    protected Integer eventTypeCode = 0;      // FIXME: 2016.09.13. replace this const
    protected Integer statusCode = 0;         // FIXME: 2016.09.13. replace this const
    protected Date tsSpecified;
    protected Date tsRecorded;
    protected Date tsReceived;
    protected Date tsUpdated;
    protected Date tsDeleted;
    protected Device sourceDevice;

    public Observation() {
        this(null, null, null, null, null, null, null);
    }

    public Observation(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated,
                       Date tsDeleted, Device sourceDevice) {
        this.episode = episode;
        this.tsSpecified = tsSpecified;
        this.tsRecorded = tsRecorded;
        this.tsReceived = tsReceived;
        this.tsUpdated = tsUpdated;
        this.tsDeleted = tsDeleted;
        this.sourceDevice = sourceDevice;
    }

    public abstract ObservationType getType();

    public Long getPostgresId() {
        return postgresId;
    }

    public void setPostgresId(Long postgresId) {
        this.postgresId = postgresId;
    }

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public Integer getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(Integer eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Date getTsSpecified() {
        return tsSpecified;
    }

    public void setTsSpecified(Date tsSpecified) {
        this.tsSpecified = tsSpecified;
    }

    public Date getTsReceived() {
        return tsReceived;
    }

    public void setTsReceived(Date tsReceived) {
        this.tsReceived = tsReceived;
    }

    public Date getTsRecorded() {
        return tsRecorded;
    }

    public void setTsRecorded(Date tsRecorded) {
        this.tsRecorded = tsRecorded;
    }

    public Date getTsUpdated() {
        return tsUpdated;
    }

    public void setTsUpdated(Date tsUpdated) {
        this.tsUpdated = tsUpdated;
    }

    public Date getTsDeleted() {
        return tsDeleted;
    }

    public void setTsDeleted(Date tsDeleted) {
        this.tsDeleted = tsDeleted;
    }

    public Device getSourceDevice() {
        return sourceDevice;
    }

    public void setSourceDevice(Device sourceDevice) {
        this.sourceDevice = sourceDevice;
    }
}
