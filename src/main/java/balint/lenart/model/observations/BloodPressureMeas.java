package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BloodPressureMeas extends Observation {

    private Integer systolicData;
    private Integer diastolicData;
    private Integer pulseData;

    @Override
    public ObservationType getType() {
        return ObservationType.BLOOD_PRESSURE_RECORD;
    }

}
