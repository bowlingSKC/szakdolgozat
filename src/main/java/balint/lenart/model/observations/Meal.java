package balint.lenart.model.observations;

import balint.lenart.model.Device;
import balint.lenart.model.Episode;
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
    private int mealTypeCode;
    private Float glycLoad;
    private List<MealItem> mealItems = Lists.newArrayList();

    private Date helperDateTime;

    @Override
    public ObservationType getType() {
        return ObservationType.MEAL_LOG_RECORD;
    }

    @Override
    public void setEpisode(Episode episode) {
        super.setEpisode(episode);
        mealItems.forEach(item -> item.setEpisode(episode));
    }

    @Override
    public void setSourceDevice(Device sourceDevice) {
        super.setSourceDevice(sourceDevice);
        mealItems.forEach(item -> item.setSourceDevice(sourceDevice));
    }
}