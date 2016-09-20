package balint.lenart.model.observations.helper;

public class EventItemContent {

    private int nutrientId;
    private float quantity;

    public EventItemContent() {

    }

    public EventItemContent(int nutrientId, float quantity) {
        this.nutrientId = nutrientId;
        this.quantity = quantity;
    }

    public int getNutrientId() {
        return nutrientId;
    }

    public void setNutrientId(int nutrientId) {
        this.nutrientId = nutrientId;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
