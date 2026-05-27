package auction.common.model.item;

import auction.common.model.BaseEntity;

import java.time.Instant;

public class Item extends BaseEntity {
    public enum Status {
        WAITING,
        RUNNING,
        FINISHED
    }
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private Instant startTime;
    private Instant endTime;
    private String sellerId;
    private Status status;

    public Item(String id, String name, String description, double startingPrice, Instant startTime, Instant endTime, String sellerId) {
        super(id, Instant.now(), Instant.now());
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
        this.status = Status.WAITING;
    }

    // Constructor mới để tạo Item mà không cần ID
    public Item(String name, String description, double startingPrice, Instant startTime, Instant endTime, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
        this.status = Status.WAITING;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        touch();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getSellerId() { return sellerId; }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
        touch();
    }

    public void updateCurrentPrice(double newPrice) {
        if (newPrice > this.currentPrice) {
            this.currentPrice = newPrice;
            touch();
        }
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
        touch();
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
        touch();
    }

    public String getSellerUsername() {
        return sellerId;
    }

    public String getCategory() {
        return "Item";
    }

    public String getDisplayCreator() {
        return sellerId;
    }
}