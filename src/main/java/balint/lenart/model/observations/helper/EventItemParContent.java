package balint.lenart.model.observations.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventItemParContent {

    private long nutrientId;
    private String parameter;
    private double quantity;

}
