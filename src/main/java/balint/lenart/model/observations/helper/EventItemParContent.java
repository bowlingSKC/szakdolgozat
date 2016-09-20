package balint.lenart.model.observations.helper;

public class EventItemParContent {

    private int nutrientId;
    private String parameter;
    private float quantity;

    public EventItemParContent() {

    }

    public EventItemParContent(int nutrientId, String parameter, float quantity) {
        this.nutrientId = nutrientId;
        this.parameter = parameter;
        this.quantity = quantity;
    }

    public int getNutrientId() {
        return nutrientId;
    }

    public void setNutrientId(int nutrientId) {
        this.nutrientId = nutrientId;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
