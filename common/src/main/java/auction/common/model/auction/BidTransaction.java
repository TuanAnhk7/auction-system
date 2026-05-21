package auction.common.model.auction;

import auction.common.model.user.Bidder;
import auction.common.model.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidTransaction extends BaseEntity implements Serializable {
    private final Bidder bidder;
    private final double bidAmount;
    private final LocalDateTime timestamp;

    public BidTransaction(Bidder bidder, double bidAmount) {
        super();
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount() {
        return this.bidAmount;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
