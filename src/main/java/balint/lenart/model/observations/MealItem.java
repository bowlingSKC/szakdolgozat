package balint.lenart.model.observations;

import balint.lenart.model.observations.helper.EventItemContent;
import balint.lenart.model.observations.helper.EventItemParContent;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MealItem extends Observation {

    private int itemTypeCode;
    private Long recipeId;
    private String itemLabel;
    private Meal meal;
    private double quantity;
    private long unitId;
    private String unitLabel;

    private List<EventItemContent> itemContents = Lists.newArrayList();
    private List<EventItemParContent> itemParContents = Lists.newArrayList();

    private String helperMongoId;

    @Override
    public ObservationType getType() {
        return ObservationType.MEAL_LOG_RECORD;
    }
}
