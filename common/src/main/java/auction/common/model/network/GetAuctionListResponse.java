package auction.common.model.network;

import java.io.Serializable;
import java.util.List;

public class GetAuctionListResponse implements Serializable {
    private final List<AuctionView> auctions;

    public GetAuctionListResponse(List<AuctionView> auctions) {
        this.auctions = List.copyOf(auctions);
    }

    public List<AuctionView> getAuctions() {
        return auctions;
    }
}
