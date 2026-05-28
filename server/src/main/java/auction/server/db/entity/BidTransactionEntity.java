package auction.server.db.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bid_transactions")
public class BidTransactionEntity {
    @Id
    private String id;

    private String auctionId;

    private String bidderUsername;

    private double amount;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
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