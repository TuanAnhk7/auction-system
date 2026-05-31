package auction.client.network;

import auction.common.model.network.AdminAuctionActionResponse;
import auction.common.model.network.AuctionExtendedResponse;
import auction.common.model.network.BidResponse;
import auction.common.model.network.BalanceUpdateResponse;
import auction.common.model.network.CreateAuctionResponse;
import auction.common.model.network.GetAuctionListResponse;
import auction.common.model.network.LoginResponse;
import auction.common.model.network.RegisterResponse;
import auction.common.model.network.UpdateItemResponse;
import auction.common.model.network.AutoBidResponse;

public interface Observer {
    void onBidResponse(BidResponse response);

    void onAuctionListResponse(GetAuctionListResponse response);

    default void onCreateAuctionResponse(CreateAuctionResponse response) {
    }

    default void onAdminAuctionActionResponse(AdminAuctionActionResponse response) {
    }

    default void onLoginResponse(LoginResponse response) {
    }

    default void onRegisterResponse(RegisterResponse response) {
    }

    default void onUpdateItemResponse(UpdateItemResponse response) {
    }

    default void onAutoBidResponse(AutoBidResponse response) {
    }

    default void onBalanceUpdateResponse(BalanceUpdateResponse response) {
    }

    default void onAuctionExtendedResponse(AuctionExtendedResponse response) {
    }
}
