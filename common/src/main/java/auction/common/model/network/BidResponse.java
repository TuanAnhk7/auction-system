package auction.common.model.network;

import auction.common.model.item.Item;

import java.io.Serializable;

public class BidResponse implements Serializable {
    private final boolean success;
    private final String message;
    private final Item updatedItem;

    public BidResponse(boolean success, String message, Item updatedItem) {
        this.success = success;
        this.message = message;
        this.updatedItem = updatedItem;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Item getUpdatedItem() {
        return updatedItem;
    }
}
