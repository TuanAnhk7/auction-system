package auction.common.model.network;

import auction.common.model.item.Item;

import java.io.Serializable;
import java.util.List;

public class GetAuctionListResponse implements Serializable {
    private final List<Item> items;

    public GetAuctionListResponse(List<Item> items) {
        this.items = List.copyOf(items);
    }

    public List<Item> getItems() {
        return items;
    }
}
