package balint.lenart.model.observations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Anamnesis extends Observation {

    private Integer potgresId;
    private Integer height;
    private Float weight;
    private Date birthDate;
    private Integer genderCode;
    private Integer lifestyleCode;
    private Integer sportCode;
    private Float massChange;
    private Integer massChangeTime;
    private Float egfr;
    private Boolean steroidTreatment;
    private Float insulinDose;

    @Override
    public ObservationType getType() {
        return ObservationType.DIETLOG_ANAM_RECORD;
    }
}
