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
public class MealItem {

    private Long postgresId;
    private int itemTypeCode;
    private Long foodId;
    private Long recipdeId;
    private String itemLabel;
    private Meal meal;
    private float quantity;
    private long unitId;
    private String unitLabel;

    private List<EventItemContent> itemContents = Lists.newArrayList();
    private List<EventItemParContent> itemParContents = Lists.newArrayList();

}
