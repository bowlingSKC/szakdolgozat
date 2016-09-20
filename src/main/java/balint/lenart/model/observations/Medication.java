package balint.lenart.model.observations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Medication extends Observation {

    private int medicationId;
    private double quantity;
    private Integer unitId;
    private String unitLabel;
    private Integer adminRouteCode;
    private Integer adminLocCode;
    private Integer relatedMealId;
    private Integer mealRelatedTypeCode;
    private Integer relatedMealTypeCode;

    @Override
    public ObservationType getType() {
        return ObservationType.MEDICATION_RECORD;
    }

}
