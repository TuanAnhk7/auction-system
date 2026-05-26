package auction.common.model.network;

import java.io.Serializable;

public class UpdateItemResponse implements Serializable {//trả dữ liệu đã sửa đổi từ server về client
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private AuctionView updatedAuction;

    public UpdateItemResponse(boolean success, String message, AuctionView updatedAuction) {
        this.success = success;
        this.message = message;
        this.updatedAuction = updatedAuction;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AuctionView getUpdatedAuction() {
        return updatedAuction;
    }
}