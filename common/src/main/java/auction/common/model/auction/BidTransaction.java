package auction.common.model.auction;

import auction.common.model.BaseEntity;
import auction.common.model.user.Bidder;

import java.time.Instant;
import java.time.LocalDateTime;

public class BidTransaction extends BaseEntity {
    private final String bidderId;
    private final String bidderUsername;
    private final double amount;

    // Constructor mới để lưu trữ ID và username của Bidder
    public BidTransaction(Bidder bidder, double amount) {
        super();
        if (bidder == null) {
            throw new IllegalArgumentException("Bidder cannot be null.");
        }
        this.bidderId = bidder.getId();
        this.bidderUsername = bidder.getUsername();
        this.amount = amount;
    }

    public BidTransaction(String id, Instant createdAt, Instant lastModified, String bidderId, String bidderUsername, double amount) {
        super(id, createdAt, lastModified);
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.amount = amount;
    }

    public String getBidderId() { return bidderId; }
    public String getBidderUsername() { return bidderUsername; }
    public double getAmount() { return amount; }

}