package uet.models.business;

import uet.models.user.Entity;
import uet.models.items.Item;
import uet.models.user.Bidder;
import uet.support.AuctionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity implements Serializable {
    private Item item;
    private double currentHighestBid;
    private Bidder highestBidder;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private List<BidTransaction> bidHistory;

    public Auction(Item item, LocalDateTime endTime) {
        super();
        this.item = item;
        this.currentHighestBid = item.getStartingPrice(); // Lấy giá khởi điểm từ Item
        this.endTime = endTime;
        this.status = AuctionStatus.Open;
        this.bidHistory = new ArrayList<>();
    }

    // Hàm thêm lượt đặt giá mới
    public void addBid(BidTransaction transaction) {
        this.bidHistory.add(transaction);
        this.currentHighestBid = transaction.getBidAmount();
        this.highestBidder = transaction.getBidder();
    }

    // Getters và Setters
    public Item getItem() { return item; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public Bidder getHighestBidder() { return highestBidder; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public List<BidTransaction> getBidHistory() { return bidHistory; }
}