package auction.common.model.network;

import java.io.Serializable;

public class UpdateItemRequest implements Serializable { //đóng gói dữ liệu muốn sửa đổi (client gửi lên server)
    private static final long serialVersionUID = 1L;

    private String auctionId;
    private String name;
    private String description;
    private double startingPrice;
    private String itemType;
    private String specificProp1;
    private int specificProp2;

    public UpdateItemRequest(String auctionId, String name, String description, double startingPrice, String itemType, String specificProp1, int specificProp2) {
        this.auctionId = auctionId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.itemType = itemType;
        this.specificProp1 = specificProp1;
        this.specificProp2 = specificProp2;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public String getItemType() {
        return itemType;
    }

    public String getSpecificProp1() {
        return specificProp1;
    }

    public int getSpecificProp2() {
        return specificProp2;
    }
}