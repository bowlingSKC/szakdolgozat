package balint.lenart.model.observations;

import balint.lenart.model.observations.helper.EventAnamnesisIllness;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Anamnesis extends Observation {

    private Integer height;
    private Double weight;
    private Date birthDate;
    private Integer genderCode;
    private Integer lifestyleCode;
    private Integer sportCode;
    private Double massChange;
    private Integer massChangeTime;
    private Double egfr;
    private Boolean steroidTreatment;
    private Double insulinDose;
    private List<EventAnamnesisIllness> illnesses = Lists.newArrayList();

    @Override
    public ObservationType getType() {
        return ObservationType.DIETLOG_ANAM_RECORD;
    }
}
