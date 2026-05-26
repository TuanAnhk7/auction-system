package auction.common.model.network;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CreateAuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L; 

    private final String sellerUsername;
    private final String itemName;
    private final double startingPrice;
    private final String description;
    private final String itemType;
    private final LocalDateTime startTime;
    private final long durationMinutes;
    private final String specificProp1;
    private final int specificProp2;

    public CreateAuctionRequest(
            String sellerUsername,
            String itemName,
            double startingPrice,
            String description,
            String itemType,
            LocalDateTime startTime,
            long durationMinutes,
            String specificProp1,
            int specificProp2
    ) {
        this.sellerUsername = sellerUsername;
        this.itemName = itemName;
        this.startingPrice = startingPrice;
        this.description = description;
        this.itemType = itemType;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.specificProp1 = specificProp1;
        this.specificProp2 = specificProp2;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public String getSpecificProp1() {
        return specificProp1;
    }

    public int getSpecificProp2() {
        return specificProp2;
    }
}