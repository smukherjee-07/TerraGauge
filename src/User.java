import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    public User(int userId, String username, String email, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public User(String username, String email) {
        this(0, username, email, LocalDateTime.now());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("User [ID: %d, Username: %s, Email: %s]", userId, username, email);
    }
}