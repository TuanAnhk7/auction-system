package auction.server.db.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String username;

    private String password;

    private String role; // ADMIN, SELLER, BIDDER

    private double accountBalance;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
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