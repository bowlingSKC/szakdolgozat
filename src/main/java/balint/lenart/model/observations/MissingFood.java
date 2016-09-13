package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;

import java.util.Date;

public class MissingFood extends Observation {

    private Integer foodId;
    private Integer recipeId;
    private String messageText;

    public MissingFood() {

    }

    public MissingFood(Episode episode, Date tsSpecified, Date tsRecorded, Date tsReceived, Date tsUpdated, Date tsDeleted,
                       Device sourceDevice, Integer foodId, Integer recipeId, String messageText) {
        super(episode, tsSpecified, tsRecorded, tsReceived, tsUpdated, tsDeleted, sourceDevice);
        this.foodId = foodId;
        this.recipeId = recipeId;
        this.messageText = messageText;
    }

    @Override
    public ObservationType getType() {
        return ObservationType.NOTIFICATION_RECORD;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public Integer getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
