package auction.server.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "bid_transactions")
public class BidTransactionEntity {
    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "auction_id", nullable = false)
    private String auctionId;

    @Column(name = "bidder_username", nullable = false)
    private String bidderUsername;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "created_at")
    private String createdAt;

    // Constructor mặc định cho JPA
    public BidTransactionEntity() {
    }

    // Constructor đầy đủ tham số
    public BidTransactionEntity(String id, String auctionId, String bidderUsername, double amount, String createdAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderUsername = bidderUsername;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null || createdAt.isBlank()) {
            createdAt = LocalDateTime.now().format(DB_TIMESTAMP_FORMAT);
        }
    }

    // --- Các hàm Getter và Setter thuần túy ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public void setBidderUsername(String bidderUsername) {
        this.bidderUsername = bidderUsername;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
