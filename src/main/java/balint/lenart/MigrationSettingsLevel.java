package balint.lenart;

import balint.lenart.model.helper.NamedEnum;

public enum MigrationSettingsLevel implements NamedEnum {

    USER("Felhasználó", 1),
    DEVICE("Eszköz", 2),
    OBSERVATION("Megfigyelés", 3);

    private final String name;
    private final int level;

    MigrationSettingsLevel(final String name, final int level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isLowerOrEqualsLevel(MigrationSettingsLevel checkedLevel) {
        return checkedLevel.level <= this.level;
    }

    public boolean isLowerLevel(MigrationSettingsLevel checkedLevel) {
        return checkedLevel.level < this.level;
    }
}
