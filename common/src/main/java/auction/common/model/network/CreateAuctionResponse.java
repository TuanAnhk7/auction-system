package auction.common.model.network;

import java.io.Serializable;

public class CreateAuctionResponse implements Serializable {
    private final boolean success;
    private final String message;
    private final AuctionView createdAuction;

    public CreateAuctionResponse(boolean success, String message, AuctionView createdAuction) {
        this.success = success;
        this.message = message;
        this.createdAuction = createdAuction;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AuctionView getCreatedAuction() {
        return createdAuction;
    }
}
