package balint.lenart.model.observations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MissingFood extends Observation {

    private Integer foodId;
    private Integer recipeId;
    private String messageText;

   @Override
    public ObservationType getType() {
        return ObservationType.NOTIFICATION_RECORD;
    }

}
