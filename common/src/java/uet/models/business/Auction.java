package uet.models.business;

import uet.models.user.Entity;
import uet.models.items.Item;
import uet.models.user.Bidder;
import uet.support.AuctionStatus;
import uet.support.InvalidBidException;

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

    private transient List<Observer> observers = new ArrayList<>();

    public Auction(Item item, LocalDateTime endTime) {
        super();
        this.item = item;
        this.currentHighestBid = item.getStartingPrice(); // Lấy giá khởi điểm từ Item
        this.endTime = endTime;
        this.status = AuctionStatus.Open;
        this.bidHistory = new ArrayList<>();
    }

    public synchronized void placeBid(Bidder bidder, double bidAmount) throws InvalidBidException {
        if (this.status != AuctionStatus.Running) {
            throw new InvalidBidException("Phiên đấu giá đang đóng.");
        }

        if (bidAmount <= currentHighestBid) {
            throw new InvalidBidException("Giá đặt phải cao hơn giá hiện tại!");
        }

        // Thay thế logic của addBid cũ
        this.currentHighestBid = bidAmount;
        this.highestBidder = bidder;
        this.bidHistory.add(new BidTransaction(bidder, bidAmount, LocalDateTime.now()));

        notifyObservers();//báo interface để cập nhật số
    }

    public double getCurrentHighestBid() { return currentHighestBid; }
    public Item getItem() { return item; }
    public AuctionStatus getStatus() { return status; }

    public void addObserver(Observer o) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }
        this.observers.add(o);
    }

    private void notifyObservers() {
        if (observers != null) {
            for (Observer o : observers) {
                o.update(this);
            }
        }
    }
}