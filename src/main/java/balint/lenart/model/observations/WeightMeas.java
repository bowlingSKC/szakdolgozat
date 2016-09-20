package balint.lenart.model.observations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WeightMeas extends Observation {

    private Double weightData;

    @Override
    public ObservationType getType() {
        return ObservationType.WEIGHT_RECORD;
    }

}
