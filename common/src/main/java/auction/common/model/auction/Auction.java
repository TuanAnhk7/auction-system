package auction.common.model.auction;

import auction.common.exception.AuctionException;
import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.BaseEntity;
import auction.common.model.item.Item;
import auction.common.model.user.Bidder;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Auction extends BaseEntity {
    private final Item item;
    private final String sellerUsername;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<BidTransaction> bidHistory;
    private double currentHighestBid;
    private Bidder highestBidder;
    private AuctionStatus status;

    private final List<AuctionObserver> observers = new ArrayList<>();
    public Auction(Item item, LocalDateTime startTime, LocalDateTime endTime, String sellerUsername) {
        super();
        if (item == null) throw new IllegalArgumentException("Item cannot be null.");
        if (startTime == null) throw new IllegalArgumentException("Start time cannot be null.");
        if (endTime == null) throw new IllegalArgumentException("End time cannot be null.");
        if (endTime.isBefore(startTime)) throw new IllegalArgumentException("End time cannot be before start time.");

        this.item = item;
        this.sellerUsername = sellerUsername;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidHistory = new ArrayList<>();
        this.currentHighestBid = item.getStartingPrice();
        this.status = (startTime.isAfter(LocalDateTime.now())) ? AuctionStatus.PENDING : AuctionStatus.OPEN;
    }

    // Convenience constructor: start now and seller taken from item
    public Auction(Item item, LocalDateTime endTime) {
        this(item, LocalDateTime.now(), endTime, item.getSellerUsername());
    }

    public Auction(String id, Instant createdAt, Instant lastModified, Item item, String sellerUsername, LocalDateTime startTime, LocalDateTime endTime, List<BidTransaction> bidHistory, double currentHighestBid, Bidder highestBidder, AuctionStatus status) {
        super(id, createdAt, lastModified);
        this.item = item;
        this.sellerUsername = sellerUsername;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidHistory = new ArrayList<>(bidHistory); // Defensive copy
        this.currentHighestBid = currentHighestBid;
        this.highestBidder = highestBidder;
        this.status = status;
    }

    public synchronized void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public synchronized void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(BidTransaction transaction) {
        for (AuctionObserver observer : observers) {
            // Thông báo cho từng observer về giao dịch mới nhất
            observer.updatePrice(transaction.getAmount());
            observer.onNewBid(transaction);
        }
    }

    public synchronized BidTransaction placeBid(Bidder bidder, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        updateStatusIfExpired();
        if (status == AuctionStatus.FINISHED) {
            throw new AuctionClosedException("Auction has finished.");
        }
        if (status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Auction is not accepting bids.");
        }
        if (bidAmount <= currentHighestBid) {
            throw new InvalidBidException("Bid must be higher than the current highest bid.");
        }
        if (bidder.getAccountBalance() < bidAmount) {
            throw new InvalidBidException("Bidder balance is not enough for this bid.");
        }

        BidTransaction transaction = new BidTransaction(bidder, bidAmount);
        bidHistory.add(transaction);
        currentHighestBid = bidAmount;
        highestBidder = bidder;
        item.updateCurrentPrice(bidAmount);
        touch();
        notifyObservers(transaction);
        return transaction;
    }

    public synchronized void startAuction() throws AuctionException {
        if (status != AuctionStatus.OPEN && status != AuctionStatus.PENDING) {
            throw new AuctionException("Auction can only start from OPEN or PENDING status.");
        }
        this.startTime = LocalDateTime.now();
        this.status = AuctionStatus.RUNNING;
        touch();
    }

    public synchronized void finishAuction() throws AuctionException {
        if (status != AuctionStatus.RUNNING && status != AuctionStatus.OPEN) {
            throw new AuctionException("Auction can only finish from RUNNING or OPEN status.");
        }
        this.status = AuctionStatus.FINISHED;
        touch();
    }

    public synchronized void markAsPaid() throws AuctionException {
        ensureStatus(AuctionStatus.FINISHED, "Auction can only be marked as PAID from FINISHED status.");
        this.status = AuctionStatus.PAID;
        touch();
    }

    public synchronized void cancel() throws AuctionException {
        if (status == AuctionStatus.CANCELED) {
            throw new AuctionException("Auction has already been canceled.");
        }
        if (status == AuctionStatus.PAID) {
            throw new AuctionException("Auction cannot be canceled after payment.");
        }
        this.status = AuctionStatus.CANCELED;
        touch();
    }

    // Trả về true nếu phiên vừa được tự động đóng do hết giờ.
    public synchronized boolean updateStatusIfExpired() {
        boolean statusChanged = false;
        LocalDateTime now = LocalDateTime.now();

        if (status == AuctionStatus.PENDING && startTime != null && now.isAfter(startTime)) {
            this.status = AuctionStatus.RUNNING;
            statusChanged = true;
touch();
        }

        if (status == AuctionStatus.RUNNING && now.isAfter(endTime)) {
            this.status = AuctionStatus.FINISHED;
            statusChanged = true;
            touch();
        }
        return statusChanged;
    }

    private void ensureStatus(AuctionStatus expectedStatus, String message) throws AuctionException {
        if (status != expectedStatus) {
            throw new AuctionException(message);
        }
    }

    public synchronized boolean canAcceptBids() {
        updateStatusIfExpired();
        return status == AuctionStatus.RUNNING;
    }

    public Item getItem() {
        return item;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }
    
    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
        touch();
    }

    public Bidder getHighestBidder() {
        return highestBidder;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public List<BidTransaction> getBidHistory() {
        return Collections.unmodifiableList(bidHistory);
    }
}