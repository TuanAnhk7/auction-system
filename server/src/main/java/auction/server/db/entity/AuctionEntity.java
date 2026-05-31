package auction.server.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "auctions")
public class AuctionEntity {
    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "seller_username", nullable = false)
    private String sellerUsername;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "current_highest_bid", nullable = false)
    private double currentHighestBid;

    @Column(name = "highest_bidder_username")
    private String highestBidderUsername;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "last_modified")
    private String lastModified;

    @Transient
    private String title;

    @Transient
    private String description;

    @Transient
    private double startingPrice;

    @Transient
    private double currentPrice;

    public AuctionEntity() {
    }

    public AuctionEntity(
            String id,
            String itemId,
            String sellerUsername,
            String startTime,
            String endTime,
            double currentHighestBid,
            String highestBidderUsername,
            String status,
            String createdAt,
            String lastModified
    ) {
        this.id = id;
        this.itemId = itemId;
        this.sellerUsername = sellerUsername;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = currentHighestBid;
        this.highestBidderUsername = highestBidderUsername;
        this.status = status;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.currentPrice = currentHighestBid;
        this.startingPrice = currentHighestBid;
    }

    public AuctionEntity(
            String id,
            String title,
            String description,
            double startingPrice,
            double currentPrice,
            String startTime,
            String endTime,
            String status
    ) {
        this.id = id;
        this.itemId = id;
        this.sellerUsername = "seller";
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = currentPrice;
        this.highestBidderUsername = null;
        this.status = status;
        this.createdAt = null;
        this.lastModified = null;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
    }

    @PrePersist
    private void prePersist() {
        String now = LocalDateTime.now().format(DB_TIMESTAMP_FORMAT);
        if (createdAt == null || createdAt.isBlank()) {
            createdAt = now;
        }
        if (lastModified == null || lastModified.isBlank()) {
            lastModified = now;
        }
        currentPrice = currentHighestBid;
        if (startingPrice == 0.0) {
            startingPrice = currentHighestBid;
        }
    }

    @PreUpdate
    private void preUpdate() {
        lastModified = LocalDateTime.now().format(DB_TIMESTAMP_FORMAT);
        currentPrice = currentHighestBid;
    }

    @PostLoad
    private void postLoad() {
        currentPrice = currentHighestBid;
        if (startingPrice == 0.0) {
            startingPrice = currentHighestBid;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
        this.currentPrice = currentHighestBid;
    }

    public String getHighestBidderUsername() {
        return highestBidderUsername;
    }

    public void setHighestBidderUsername(String highestBidderUsername) {
        this.highestBidderUsername = highestBidderUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        this.currentHighestBid = currentPrice;
    }
}
