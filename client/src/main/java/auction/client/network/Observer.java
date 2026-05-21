package auction.client.network;

import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListResponse;

public interface Observer {
    void onBidResponse(BidResponse response);

    void onAuctionListResponse(GetAuctionListResponse response);
}
