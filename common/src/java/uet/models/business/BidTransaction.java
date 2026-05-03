package src.java.uet.models.business;

import src.java.uet.models.user.Bidder;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidTransaction implements Serializable {
    private Bidder bidder;
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidTransaction(Bidder bidder, double bidAmount) {
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now(); // Tự động lấy giờ hiện tại hệ thống
    }

    // Các hàm Getters
    public Bidder getBidder() { return bidder; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}