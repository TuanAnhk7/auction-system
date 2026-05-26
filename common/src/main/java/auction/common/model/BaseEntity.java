package auction.common.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseEntity {
    protected final String id;
    protected Instant createdAt;
    protected Instant lastModified;

    public BaseEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.lastModified = this.createdAt;
    }

    // Constructor cho việc tải từ DataBase
    public BaseEntity(String id, Instant createdAt, Instant lastModified) {
        this.id = id;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
    }

    protected void touch() {
        this.lastModified = Instant.now();
    }

    public String getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastModified() { return lastModified; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}