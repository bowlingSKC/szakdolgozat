package balint.lenart.model;

import java.util.Date;

public class Episode {

    private Long postgresId;
    private final User user;
    private final Date startDate;
    private Device device;

    public Episode(User user, Date startDate) {
        this.user = user;
        this.startDate = startDate;
    }

    public Long getPostgresId() {
        return postgresId;
    }

    public void setPostgresId(Long postgresId) {
        this.postgresId = postgresId;
    }

    public User getUser() {
        return user;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
