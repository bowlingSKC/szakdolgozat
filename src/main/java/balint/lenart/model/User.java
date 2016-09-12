package balint.lenart.model;

public class User {

    private final String mongoId;
    private Long postgresId;
    private final boolean isActive;
    private final String comment;
    private final String email;
    private final String fullName;
    private final String password;
    private final String type;

    public User() {
        this(null, false, null, null, null, null, null);
    }

    public User(String mongoId, boolean isActive, String comment, String email, String fullName, String password, String type) {
        this.mongoId = mongoId;
        this.isActive = isActive;
        this.comment = comment;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.type = type;
    }

    public void setPostgresId(Long postgresId) {
        this.postgresId = postgresId;
    }

    public Long getPostgresId() {
        return postgresId;
    }

    public String getMongoId() {
        return mongoId;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getComment() {
        return comment;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }
}
