package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class Medication extends Observation {

    private int medicationId;
    private float quantity;
    private Integer unitId;
    private String unitLabel;
    private Integer adminRouteCode;
    private Integer adminLocCode;
    private Integer relatedMealId;
    private Integer mealRelatedTypeCode;
    private Integer relatedMealTypeCode;

    public Medication() {

    }

    public Medication(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated, Date tsDeleted,
                      Device sourceDevice, int medicationId, float quantity, Integer unitId, String unitLabel,
                      Integer adminRouteCode, Integer adminLocCode, Integer relatedMealId, Integer mealRelatedTypeCode,
                      Integer relatedMealTypeCode) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.medicationId = medicationId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.unitLabel = unitLabel;
        this.adminRouteCode = adminRouteCode;
        this.adminLocCode = adminLocCode;
        this.relatedMealId = relatedMealId;
        this.mealRelatedTypeCode = mealRelatedTypeCode;
        this.relatedMealTypeCode = relatedMealTypeCode;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.MEDICATION_RECORD;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }

    public Integer getAdminRouteCode() {
        return adminRouteCode;
    }

    public void setAdminRouteCode(Integer adminRouteCode) {
        this.adminRouteCode = adminRouteCode;
    }

    public Integer getAdminLocCode() {
        return adminLocCode;
    }

    public void setAdminLocCode(Integer adminLocCode) {
        this.adminLocCode = adminLocCode;
    }

    public Integer getRelatedMealId() {
        return relatedMealId;
    }

    public void setRelatedMealId(Integer relatedMealId) {
        this.relatedMealId = relatedMealId;
    }

    public Integer getMealRelatedTypeCode() {
        return mealRelatedTypeCode;
    }

    public void setMealRelatedTypeCode(Integer mealRelatedTypeCode) {
        this.mealRelatedTypeCode = mealRelatedTypeCode;
    }

    public Integer getRelatedMealTypeCode() {
        return relatedMealTypeCode;
    }

    public void setRelatedMealTypeCode(Integer relatedMealTypeCode) {
        this.relatedMealTypeCode = relatedMealTypeCode;
    }
}
