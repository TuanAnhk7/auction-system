package auction.common.model.auction;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.BaseEntity;
import auction.common.model.item.Item;
import auction.common.model.user.Bidder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Auction extends BaseEntity {
    private final Item item;
    private final LocalDateTime endTime;
    private final List<BidTransaction> bidHistory;
    private double currentHighestBid;
    private Bidder highestBidder;
    private AuctionStatus status;

    public Auction(Item item, LocalDateTime endTime) {
        this.item = item;
        this.endTime = endTime;
        this.bidHistory = new ArrayList<>();
        this.currentHighestBid = item.getStartingPrice();
        this.status = AuctionStatus.OPEN;
    }

    public synchronized BidTransaction placeBid(Bidder bidder, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        if (status != AuctionStatus.OPEN || LocalDateTime.now().isAfter(endTime)) {
            status = AuctionStatus.CLOSED;
            throw new AuctionClosedException("Auction is closed.");
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
        return transaction;
    }

    public synchronized void close() {
        this.status = AuctionStatus.CLOSED;
        touch();
    }

    public Item getItem() {
        return item;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
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
