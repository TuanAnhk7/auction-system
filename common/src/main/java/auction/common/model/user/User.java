package auction.common.model.user;

import auction.common.model.network.Role;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class User {
    protected final String id;
    protected String username;
    protected String hashedPassword;
    protected String fullName;
    protected Role role;
    protected Instant createdAt;
    protected Instant lastModified;
    protected boolean isActive;

    public User(String username, String hashedPassword, String fullName, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = Instant.now();
        this.lastModified = this.createdAt;
        this.isActive = true;
    }

    public User(String id, String username, String hashedPassword, String fullName, Role role, Instant createdAt, Instant lastModified, boolean isActive) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.isActive = isActive;
    }

    protected void touch() {
        this.lastModified = Instant.now();
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastModified() { return lastModified; }
    public boolean isActive() { return isActive; }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        touch();
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        touch();
    }

    public void setActive(boolean active) {
        isActive = active;
        touch();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}