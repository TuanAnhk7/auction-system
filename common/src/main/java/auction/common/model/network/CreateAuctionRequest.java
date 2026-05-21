package auction.common.model.network;

import java.io.Serializable;

public class CreateAuctionRequest implements Serializable {
    private final String sellerUsername;
    private final String itemName;
    private final double startingPrice;
    private final String description;
    private final String itemType;
    private final long durationMinutes;

    public CreateAuctionRequest(
            String sellerUsername,
            String itemName,
            double startingPrice,
            String description,
            String itemType,
            long durationMinutes
    ) {
        this.sellerUsername = sellerUsername;
        this.itemName = itemName;
        this.startingPrice = startingPrice;
        this.description = description;
        this.itemType = itemType;
        this.durationMinutes = durationMinutes;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public String getItemName() {
        return itemName;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getItemType() {
        return itemType;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }
}
