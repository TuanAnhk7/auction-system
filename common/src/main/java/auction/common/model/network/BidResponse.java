package auction.common.model.network;

import java.io.Serializable;

public class BidResponse implements Serializable {
    private final boolean success;
    private final String message;
    private final AuctionView updatedAuction;
    private final String bidderUsername;
    private final double balance;

    public BidResponse(boolean success, String message, AuctionView updatedAuction) {
        this(success, message, updatedAuction, null);
    }

    public BidResponse(boolean success, String message, AuctionView updatedAuction, String bidderUsername) {
        this(success, message, updatedAuction, bidderUsername, 0.0);
    }

    public BidResponse(boolean success, String message, AuctionView updatedAuction, String bidderUsername, double balance) {
        this.success = success;
        this.message = message;
        this.updatedAuction = updatedAuction;
        this.bidderUsername = bidderUsername;
        this.balance = balance;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AuctionView getUpdatedAuction() {
        return updatedAuction;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public double getBalance() {
        return balance;
    }
}
