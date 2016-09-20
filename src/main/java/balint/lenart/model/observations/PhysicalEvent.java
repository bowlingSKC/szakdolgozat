package balint.lenart.model.observations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PhysicalEvent extends Observation {

    private int paId;
    private String paLabel;
    private Integer duration;
    private Integer energyConsumed;

    @Override
    public ObservationType getType() {
        return ObservationType.PA_LOG_RECORD;
    }
}
