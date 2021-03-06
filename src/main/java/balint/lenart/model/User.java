package balint.lenart.model;

public class User {

    private String mongoId;
    private Long postgresId;
    private boolean isActive;
    private String comment;
    private String email;
    private String fullName;
    private String password;
    private String type;

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

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(String type) {
        this.type = type;
    }
}
