package auction.server.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "users")
public class UserEntity {
    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role; // ADMIN, SELLER, BIDDER

    @Column(name = "account_balance", nullable = false)
    private double accountBalance;

    @Column(name = "created_at")
    private String createdAt;

    // 1. Constructor mặc định bắt buộc cho JPA/Hibernate
    public UserEntity() {
    }

    // 2. Constructor đầy đủ tham số
    public UserEntity(String username, String password, String role, double accountBalance, String createdAt) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountBalance = accountBalance;
        this.createdAt = createdAt;
    }

    // 3. Custom Constructor khớp với logic gọi từ UserManager
    public UserEntity(String username, String password, String role, double accountBalance, Object dummy) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountBalance = accountBalance;
        this.createdAt = null;
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null || createdAt.isBlank()) {
            createdAt = LocalDateTime.now().format(DB_TIMESTAMP_FORMAT);
        }
    }

    // --- Các hàm Getter và Setter thuần túy ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
