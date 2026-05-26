package auction.common.model.network;

import java.io.Serializable;

// thông báo ng thắng phiên đấu giá đã kết thúc
public class AuctionEndedResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final String winnerUsername;
    private final double finalBidAmount;
    private final String message;

    public AuctionEndedResponse(String auctionId, String itemName, String winnerUsername, double finalBidAmount, String message) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.winnerUsername = winnerUsername;
        this.finalBidAmount = finalBidAmount;
        this.message = message;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public double getFinalBidAmount() {
        return finalBidAmount;
    }

    public String getMessage() {
        return message;
    }
}