package auction.common.model.network;

import auction.common.model.auction.AuctionStatus;
import java.io.Serializable;

public class ChangeStatusRequest implements Serializable {
    private final String itemId;
    private final AuctionStatus newStatus;

    public ChangeStatusRequest(String itemId, AuctionStatus newStatus) {
        this.itemId = itemId;
        this.newStatus = newStatus;
    }

    public String getItemId() {
        return itemId;
    }

    public AuctionStatus getNewStatus() {
        return newStatus;
    }
}