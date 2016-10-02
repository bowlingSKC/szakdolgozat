package balint.lenart.log;

import balint.lenart.model.helper.NamedEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Log {

    public enum LogType implements NamedEnum {
        APPLICATION("Alkalmazás logok"), MIGRATION("Migráció logok");

        private final String name;

        LogType(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private String fileName;
    private String timeString;
    private String content;

}
