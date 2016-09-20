package balint.lenart.model.observations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GlucoseMeas extends Observation {

    private Integer measTimeCode;
    private Double glucoseData;

    @Override
    public ObservationType getType() {
        return ObservationType.BLOOD_GLUCOSE_RECORD;
    }


}
