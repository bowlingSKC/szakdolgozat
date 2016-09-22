package balint.lenart.model.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationElement {

    public static enum EntityType implements NamedEnum {
        USER("Felhasználó"), DEVICE("Eszköz");

        private final String name;

        EntityType(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private Date time;
    private NamedEnum entityType;       // can be EntityType or ObservationType
    private boolean success;
    private Throwable exception;

    public String getExceptionStackTraceString() {
        return exception != null ? ExceptionUtils.getStackTrace(exception) : null;
    }

}
