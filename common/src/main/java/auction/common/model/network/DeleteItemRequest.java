package auction.common.model.network;

import java.io.Serializable;

public class DeleteItemRequest implements Serializable { //gửi yêu cầu xóa đi
    private static final long serialVersionUID = 1L;

    private String auctionId;

    public DeleteItemRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}