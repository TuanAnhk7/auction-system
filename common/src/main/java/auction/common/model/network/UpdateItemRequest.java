package auction.common.model.network;

import java.io.Serializable;

public class UpdateItemRequest implements Serializable {
    private final String itemId;
    private final String newName;
    private final double newPrice;
    private final String newDescription;

    public UpdateItemRequest(String itemId, String newName, double newPrice, String newDescription) {
        this.itemId = itemId;
        this.newName = newName;
        this.newPrice = newPrice;
        this.newDescription = newDescription;
    }

    public String getItemId() {
        return itemId;
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