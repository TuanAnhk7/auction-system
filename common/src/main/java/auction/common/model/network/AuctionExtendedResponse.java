package auction.common.model.network;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionExtendedResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final String auctionId;
    private final LocalDateTime newEndTime;
    private final AuctionView updatedAuction;

    public AuctionExtendedResponse(boolean success, String message, String auctionId, LocalDateTime newEndTime, AuctionView updatedAuction) {
        this.success = success;
        this.message = message;
        this.auctionId = auctionId;
        this.newEndTime = newEndTime;
        this.updatedAuction = updatedAuction;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public LocalDateTime getNewEndTime() {
        return newEndTime;
    }

    public AuctionView getUpdatedAuction() {
        return updatedAuction;
    }
}
