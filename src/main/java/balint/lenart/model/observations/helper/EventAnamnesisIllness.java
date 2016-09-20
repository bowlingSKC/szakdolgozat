package balint.lenart.model.observations.helper;

public class EventAnamnesisIllness {

    private long anamnesisId;
    private long illnessId;
    private String parameter;
    private Integer paramLabelId;

    public EventAnamnesisIllness() {

    }

    public EventAnamnesisIllness(long anamnesisId, long illnessId, String parameter, Integer paramLabelId) {
        this.anamnesisId = anamnesisId;
        this.illnessId = illnessId;
        this.parameter = parameter;
        this.paramLabelId = paramLabelId;
    }

    public long getAnamnesisId() {
        return anamnesisId;
    }

    public long getIllnessId() {
        return illnessId;
    }

    public String getParameter() {
        return parameter;
    }

    public Integer getParamLabelId() {
        return paramLabelId;
    }

    public void setAnamnesisId(long anamnesisId) {
        this.anamnesisId = anamnesisId;
    }

    public void setIllnessId(long illnessId) {
        this.illnessId = illnessId;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void setParamLabelId(Integer paramLabelId) {
        this.paramLabelId = paramLabelId;
    }
}
