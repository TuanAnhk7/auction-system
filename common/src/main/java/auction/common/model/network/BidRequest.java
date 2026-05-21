package auction.common.model.network;

import java.io.Serializable;

public class BidRequest implements Serializable {
    private final String itemId;
    private final String username;
    private final double bidAmount;

    public BidRequest(String itemId, String username, double bidAmount) {
        this.itemId = itemId;
        this.username = username;
        this.bidAmount = bidAmount;
    }

    public String getItemId() {
        return itemId;
    }

    public String getUsername() {
        return username;
    }

    public double getBidAmount() {
        return bidAmount;
    }
}
