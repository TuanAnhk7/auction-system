package auction.common.model.network;

import java.io.Serializable;

public class DeleteItemRequest implements Serializable {
    private final String auctionId;

    public DeleteItemRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}