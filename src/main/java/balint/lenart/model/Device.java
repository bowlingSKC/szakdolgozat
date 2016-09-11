package balint.lenart.model;

public class Device {

    private final String mongoId;
    private final String name;
    private final String description;
    private final String hwId;
    private final int devTypeCode;

    public Device(String mongoId, String name, String description, String hwId, int devTypeCode) {
        this.mongoId = mongoId;
        this.name = name;
        this.description = description;
        this.hwId = hwId;
        this.devTypeCode = devTypeCode;
    }

    public String getMongoId() {
        return mongoId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHwId() {
        return hwId;
    }

    public int getDevTypeCode() {
        return devTypeCode;
    }
}
