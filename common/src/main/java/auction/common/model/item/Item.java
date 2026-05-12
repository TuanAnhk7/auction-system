package auction.common.model.item;

import auction.common.model.BaseEntity;

public abstract class Item extends BaseEntity {
    private final String name;
    private final String description;
    private final double startingPrice;
    private double currentPrice;

    protected Item(String name, String description, double startingPrice) {
        this(java.util.UUID.randomUUID().toString(), name, description, startingPrice);
    }

    protected Item(String itemId, String name, String description, double startingPrice) {
        super(itemId);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
    }

    public abstract String getCategory();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void updateCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        touch();
    }
}
