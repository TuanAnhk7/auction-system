package auction.common.model.network;

import java.io.Serializable;

public class DeleteItemResponse implements Serializable { // phản hồi yêu cầu sau khi xóa
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;

    public DeleteItemResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}