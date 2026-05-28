package auction.common.model.network;

import java.io.Serializable;

public class AutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String username;
    private final double maxBid;
    private final double increment;

    public AutoBidRequest(String auctionId, String username, double maxBid, double increment) {
        this.auctionId = auctionId;
        this.username = username;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getUsername() {
        return username;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }
}
