package auction.common.model.network;

import auction.common.model.auction.AuctionStatus;
import java.io.Serializable;

public class ChangeStatusRequest implements Serializable {
    private final String auctionId;
    private final AuctionStatus newStatus;

    public ChangeStatusRequest(String auctionId, AuctionStatus newStatus) {
        this.auctionId = auctionId;
        this.newStatus = newStatus;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public AuctionStatus getNewStatus() {
        return newStatus;
    }
}