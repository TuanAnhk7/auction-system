package auction.common.model.network;

import java.io.Serializable;

public class UpdateItemRequest implements Serializable {
    private final String auctionId;
    private final String newName;
    private final double newPrice;
    private final String newDescription;

    public UpdateItemRequest(String auctionId, String newName, double newPrice, String newDescription) {
        this.auctionId = auctionId;
        this.newName = newName;
        this.newPrice = newPrice;
        this.newDescription = newDescription;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getNewName() {
        return newName;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public String getNewDescription() {
        return newDescription;
    }
}