package balint.lenart.model.observations;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Meal extends Observation {

    private Date tsMealEnd;
    private Integer mealTypeCode;
    private Float glycLoad;
    private List<MealItem> mealItems = Lists.newArrayList();

    @Override
    public ObservationType getType() {
        return ObservationType.MEAL_LOG_RECORD;
    }

}