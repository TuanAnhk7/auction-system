package auction.common.model.network;

import java.io.Serializable;

public class DeleteAuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String auctionId;

    public DeleteAuctionRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}
