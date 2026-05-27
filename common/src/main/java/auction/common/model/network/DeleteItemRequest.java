package auction.common.model.network;

import java.io.Serializable;

public class DeleteItemRequest implements Serializable {
    private final String itemId;

    public DeleteItemRequest(String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }
}