package auction.common.model.network;

import java.io.Serializable;

public class AdminAuctionActionRequest implements Serializable {
    private final String auctionId;
    private final String action;
    private final String adminUsername;

    public AdminAuctionActionRequest(String auctionId, String action, String adminUsername) {
        this.auctionId = auctionId;
        this.action = action;
        this.adminUsername = adminUsername;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getAction() {
        return action;
    }

    public String getAdminUsername() {
        return adminUsername;
    }
}
