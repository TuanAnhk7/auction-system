package auction.client.network;

import auction.common.model.network.AdminAuctionActionResponse;
import auction.common.model.network.BidResponse;
import auction.common.model.network.CreateAuctionResponse;
import auction.common.model.network.GetAuctionListResponse;

public interface Observer {
    void onBidResponse(BidResponse response);

    void onAuctionListResponse(GetAuctionListResponse response);

    default void onCreateAuctionResponse(CreateAuctionResponse response) {
    }

    default void onAdminAuctionActionResponse(AdminAuctionActionResponse response) {
    }
}
