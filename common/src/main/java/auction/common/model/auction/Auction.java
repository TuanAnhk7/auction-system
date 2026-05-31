package auction.common.model.auction;

import auction.common.exception.AuctionException;
import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.BaseEntity;
import auction.common.model.item.Item;
import auction.common.model.user.Bidder;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Auction extends BaseEntity {
    private static final long EXTENSION_THRESHOLD_SECONDS =
            Long.getLong("auction.extension.thresholdSeconds", 30L);
    private static final long EXTENSION_DURATION_MINUTES =
            Long.getLong("auction.extension.durationMinutes", 2L);

    private final Item item;
    private final String sellerUsername;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<BidTransaction> bidHistory;
    private double currentHighestBid;
    private Bidder highestBidder;
    private AuctionStatus status;
    private final PriorityQueue<AutoBid> autoBids = new PriorityQueue<>();

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

    public Auction(String id, Item item, LocalDateTime startTime, LocalDateTime endTime, String sellerUsername) {
        super(id, Instant.now(), Instant.now());
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

    public Auction(String id, Item item, LocalDateTime endTime) {
        this(id, item, LocalDateTime.now(), endTime, item.getSellerUsername());
    }

    public Auction(Item item, LocalDateTime endTime) {
        this(item, LocalDateTime.now(), endTime, item.getSellerUsername());
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
            observer.updatePrice(transaction.getAmount());
            observer.onNewBid(transaction);
        }
    }

    public synchronized List<BidTransaction> placeBid(Bidder bidder, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        LocalDateTime bidReceivedAt = LocalDateTime.now();
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

        List<BidTransaction> newTransactions = new ArrayList<>();
        newTransactions.add(executeBid(bidder, bidAmount));
        newTransactions.addAll(processAutoBids());
        extendEndTimeIfNeeded(bidReceivedAt);
        return newTransactions;
    }

    private BidTransaction executeBid(Bidder bidder, double bidAmount) {
        BidTransaction transaction = new BidTransaction(bidder, bidAmount);
        bidHistory.add(transaction);
        currentHighestBid = bidAmount;
        highestBidder = bidder;
        item.updateCurrentPrice(bidAmount);
        touch();
        notifyObservers(transaction);
        return transaction;
    }

    public synchronized List<BidTransaction> registerAutoBid(Bidder bidder, double maxBid, double increment) throws InvalidBidException {
        LocalDateTime requestTime = LocalDateTime.now();
        if (maxBid <= currentHighestBid) {
            throw new InvalidBidException("Max bid must be higher than current price.");
        }
        AutoBid autoBid = new AutoBid(bidder, maxBid, increment);
        autoBids.remove(autoBid);
        autoBids.add(autoBid);
        List<BidTransaction> transactions = processAutoBids();
        if (!transactions.isEmpty()) {
            extendEndTimeIfNeeded(requestTime);
        }
        return transactions;
    }

    private List<BidTransaction> processAutoBids() {
        List<BidTransaction> newTransactions = new ArrayList<>();
        boolean bidPlaced;
        do {
            bidPlaced = false;
            if (autoBids.isEmpty()) break;

            AutoBid first = autoBids.poll();
            AutoBid second = autoBids.peek();

            AutoBid bidderToAct = null;
            if (highestBidder == null || !highestBidder.equals(first.getBidder())) {
                bidderToAct = first;
            } else if (second != null && !highestBidder.equals(second.getBidder())) {
                bidderToAct = second;
            }

            if (bidderToAct != null) {
                double nextBid = currentHighestBid + bidderToAct.getIncrement();
                if (nextBid <= bidderToAct.getMaxBid()) {
                    newTransactions.add(executeBid(bidderToAct.getBidder(), nextBid));
                    bidPlaced = true;
                }
            }
            autoBids.add(first);
        } while (bidPlaced);
        return newTransactions;
    }

    public synchronized void startAuction() throws AuctionException {
        LocalDateTime actionTime = LocalDateTime.now();
        if (status != AuctionStatus.OPEN && status != AuctionStatus.PENDING) {
            throw new AuctionException("Auction can only start from OPEN or PENDING status.");
        }
        this.startTime = LocalDateTime.now();
        this.status = AuctionStatus.RUNNING;
        touch();
        List<BidTransaction> transactions = processAutoBids();
        if (!transactions.isEmpty()) {
            extendEndTimeIfNeeded(actionTime);
        }
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

    public synchronized boolean updateStatusIfExpired() {
        boolean statusChanged = false;
        LocalDateTime now = LocalDateTime.now();

        if (status == AuctionStatus.PENDING && startTime != null && now.isAfter(startTime)) {
            this.status = AuctionStatus.RUNNING;
            statusChanged = true;
            touch();
            processAutoBids();
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

    public synchronized boolean extendEndTimeIfNeeded() {
        return extendEndTimeIfNeeded(LocalDateTime.now());
    }

    public synchronized boolean extendEndTimeIfNeeded(LocalDateTime referenceTime) {
        if (status != AuctionStatus.RUNNING || endTime == null) {
            return false;
        }

        long secondsLeft = ChronoUnit.SECONDS.between(referenceTime, endTime);
        if (secondsLeft <= 0 || secondsLeft > EXTENSION_THRESHOLD_SECONDS) {
            return false;
        }

        endTime = endTime.plusMinutes(EXTENSION_DURATION_MINUTES);
        touch();
        return true;
    }

    public Item getItem() { return item; }
    public String getSellerUsername() { return sellerUsername; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
        touch();
    }
    public Bidder getHighestBidder() { return highestBidder; }
    public AuctionStatus getStatus() { return status; }
    public List<BidTransaction> getBidHistory() { return Collections.unmodifiableList(bidHistory); }
}
