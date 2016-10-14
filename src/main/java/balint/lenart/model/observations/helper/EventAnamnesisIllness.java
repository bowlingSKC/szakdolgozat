package balint.lenart.model.observations.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAnamnesisIllness {

    private long anamnesisId;
    private long illnessId;
    private String parameter;
    private Integer paramLabelId;

}
