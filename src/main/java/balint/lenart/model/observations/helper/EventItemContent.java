package balint.lenart.model.observations.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventItemContent {

    private int nutrientId;
    private double quantity;

}
