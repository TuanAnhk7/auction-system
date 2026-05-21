package auction.common.model.network;

import java.io.Serializable;

public class AdminAuctionActionResponse implements Serializable {
    private final boolean success;
    private final String message;
    private final AuctionView updatedAuction;

    public AdminAuctionActionResponse(boolean success, String message, AuctionView updatedAuction) {
        this.success = success;
        this.message = message;
        this.updatedAuction = updatedAuction;
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
}
